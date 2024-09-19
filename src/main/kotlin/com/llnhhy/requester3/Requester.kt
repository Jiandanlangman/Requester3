package com.llnhhy.requester3

import com.llnhhy.requester3.config.Config
import com.llnhhy.requester3.config.ConfigBuilder
import com.llnhhy.requester3.config.Timeout
import com.llnhhy.requester3.interceptor.Interceptor
import com.llnhhy.requester3.request.*
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import kotlin.collections.component1
import kotlin.collections.component2


object Requester {

    private var defaultDispatcher: CoroutineDispatcher? = null


    private val requesterGlobalScope = CoroutineScope(Dispatchers.Unconfined)

    private val requestTimeoutProperties = HashMap<String, Timeout>()

    private val defaultCharset = Charset.forName("UTF-8")

    internal var config: Config = Config()
        private set

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor {
            val timeOut = requestTimeoutProperties[it.request().tag()] ?: config.timeout
            it.withReadTimeout(timeOut.read, TimeUnit.MILLISECONDS)
                .withConnectTimeout(timeOut.connect, TimeUnit.MILLISECONDS)
                .withWriteTimeout(timeOut.write, TimeUnit.MILLISECONDS)
                .proceed(it.request())
        }
        .dispatcher(Dispatcher(Executors.newSingleThreadExecutor()))
        .sslSocketFactory(X509TrustManagerDelegate.getSslContext().socketFactory, X509TrustManagerDelegate)
        .hostnameVerifier(HostnameVerifierDelegate)
        .dns(DnsDelegate)
        .build()

    @OptIn(DelicateCoroutinesApi::class)
    @Synchronized
    private fun getDispatcher(): CoroutineDispatcher {
        return config.dispatcher?.apply {
            val temp = defaultDispatcher
            defaultDispatcher = null
            try {
                (temp?.asExecutor() as? ExecutorService)?.shutdown()
            } catch (ignore: Throwable) {

            }
        } ?: defaultDispatcher ?: newFixedThreadPoolContext(16, "Requester3-Work-Dispatcher").apply {
            synchronized(Requester) {
                defaultDispatcher = this
            }
        }
    }

    private val interceptor = object : Interceptor {

        override suspend fun proceed(entity: RequestEntity): Response {
            return originalCall(entity)
        }

        override fun cancel(entity: RequestEntity): Response {
            val t = System.currentTimeMillis()
            return Response(
                requestInfo = entity,
                code = Response.CODE_REQUEST_CANCELED,
                headers = emptyMap(),
                body = null,
                requestTime = t,
                responseTime = t
            )
        }


    }

    infix fun config(builder: ConfigBuilder.() -> Unit) {
        config = ConfigBuilder().apply(builder).build()
    }


    suspend fun call(entity: RequestEntity): Response {
        return config.interceptor.invoke(interceptor, entity)
    }

    fun enqueue(
        entity: RequestEntity,
        response: NotCallerThread.(Response) -> Unit
    ) = requesterGlobalScope.launch(getDispatcher()) {
        response(NotCallerThread, call(entity))
    }

    private suspend fun originalCall(entity: RequestEntity): Response {
        return withContext(getDispatcher()) {
            val requestTime = System.currentTimeMillis()
            val tag = "${System.currentTimeMillis()}-${entity.hashCode()}-${Thread.currentThread().id}"
            if (entity.timeout != config.timeout) {
                requestTimeoutProperties[tag] = entity.timeout
            }
            try {
                httpClient.newCall(
                    Request.Builder()
                        .tag(tag)
                        .url(getFullUrl(entity.url, entity.method, entity.queries, entity.formData))
                        .apply {
                            entity.headers.forEach { (t, u) ->
                                addHeader(t, u.toString())
                            }
                        }
//                        .method(entity.method, if(entity.body != null) {
//                            entity.body.let { body ->
//                                body.data.toRequestBody(body.contentType.toMediaTypeOrNull())
//                            }
//                        } else {
//                            FormBody.Builder().apply {
//                                entity.formData.forEach { (t, u) ->
//                                    add(t, u.toString())
//                                }
//                            }.build()
//                        })
                        .method(
                            entity.method,
                            when (entity.method) {
                                "GET", "HEAD" -> null
                                else -> {
                                    if (entity.body != null) {
                                        entity.body.let { body ->
                                            body.data.toRequestBody(body.contentType.toMediaTypeOrNull())
                                        }
                                    } else {
                                        FormBody.Builder().apply {
                                            entity.formData.forEach { (t, u) ->
                                                add(t, u.toString())
                                            }
                                        }.build()
                                    }
                                }
                            }

                        )
                        .build()
                ).execute().let {
                    val headers = HashMap<String, String>()
                    it.headers.toMap().forEach { (t, u) ->
                        headers[t.lowercase()] = u
                    }
                    val sourceContent = it.body?.bytes()
                    val contentLength = sourceContent?.size ?: -1
                    headers["content-length"] = contentLength.toString()
                    headers["uncompress-content-length"] = contentLength.toString()
                    val isGZipData = headers["content-encoding"]?.contains("gzip", true) == true
                    val content = if (!isGZipData) {
                        sourceContent
                    } else {
                        if (sourceContent != null) {
                            val unCompressContent = unCompressGZIPData(sourceContent)
                            headers["uncompress-content-length"] = unCompressContent.size.toString()
                            unCompressContent
                        } else {
                            null
                        }
                    }
                    it.body?.contentType()?.toString()?.let {
                        headers["content-type"] = it
                    }
                    Response(requestInfo = entity, code = it.code, headers = headers.toMap(), body = content, requestTime = requestTime, responseTime = System.currentTimeMillis())
                }
            } catch (tr: Throwable) {
                Response(
                    entity,
                    Response.CODE_REQUEST_ERROR,
                    emptyMap(),
                    null,
                    requestTime,
                    System.currentTimeMillis(),
                    tr
                )
            } finally {
                requestTimeoutProperties.remove(tag)
            }
        }
    }


    private fun getFullUrl(url: String, method: String, queries: Map<String, Any>, params: Map<String, Any>): String {
        val sb = StringBuilder()
        if (!url.startsWith("https://", true) && !url.startsWith("http://", true)) {
            sb.append(config.baseUrl)
            sb.append("/")
            sb.append(url)
        } else {
            sb.append(url)
        }
        var hasQuery = sb.contains("?")
        fun appendMap(map: Map<String, Any>) {
            map.forEach { (t, u) ->
                if (!hasQuery) {
                    sb.append("?")
                    hasQuery = true
                } else {
                    sb.append("&")
                }
                sb.append(t).append("=").append(URLEncoder.encode(u.toString(), "UTF-8"))
            }
        }
        appendMap(queries)
//        if (params.isNotEmpty() && (method == "GET" || method == "HEADER")) {
//            appendMap(params)
//        }
        return sb.toString()
    }


    private fun unCompressGZIPData(src: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        val gzis = GZIPInputStream(ByteArrayInputStream(src))
        val buffer = ByteArray(2048)
        var readLength: Int
        while (gzis.read(buffer).also { readLength = it } != -1)
            baos.write(buffer, 0, readLength)
        gzis.close()
        val result = baos.toByteArray()
        baos.close()
        return result
    }

    private fun isTextBody(contentType: String): Boolean {
        when {
            contentType.contains("json") -> {
                return true
            }

            contentType.contains("text") -> {
                return true
            }

            contentType.contains("html") -> {
                return true
            }

            contentType.contains("xml") -> {
                return true
            }
        }
        return false
    }


}