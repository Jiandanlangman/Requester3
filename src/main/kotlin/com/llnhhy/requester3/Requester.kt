package com.llnhhy.requester3

import com.llnhhy.requester3.config.Config
import com.llnhhy.requester3.config.ConfigBuilder
import com.llnhhy.requester3.config.Timeout
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

/**
 * TODO:
 *  文件上传支持
 */
object Requester {

    private var defaultDispatcher: CoroutineDispatcher? = null


    private val requesterGlobalScope = CoroutineScope(Dispatchers.Unconfined)

    private val requestTimeoutProperties = HashMap<String, Timeout>()

    private val defaultCharset = Charset.forName("UTF-8")

    var config: Config = Config()
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
    private fun getDispatcher(): CoroutineDispatcher {
        val configDispatcher = config.dispatcher
        if(configDispatcher != null) {
            if(defaultDispatcher != null) {
                val temp = defaultDispatcher
                defaultDispatcher = null
                requesterGlobalScope.launch(configDispatcher) {
                    try {
                        (temp?.asExecutor() as?  ExecutorService)?.shutdown()
                    } catch (ignore:Throwable) {

                    }
                }
            }
            return configDispatcher
        }
        val dispatcher =  defaultDispatcher
        if (dispatcher != null) {
            return dispatcher
        }
        return newFixedThreadPoolContext(16, "Requester3-Work-Dispatcher").apply {
            synchronized(Requester) {
                defaultDispatcher = this
            }
        }
    }


    fun config(builder: ConfigBuilder) {
        config = builder.build()
    }


    suspend fun call(requestEntity: RequestEntity): Response {
        return withContext(getDispatcher()) {
            val requestTime = System.currentTimeMillis()
            val newRequestEntity =
                if (requestEntity.hasProhibit(RequestEntity.PROHIBIT_FLAG_PROCESSOR)) requestEntity else config.requestProcessor(
                    NotCallerThread,
                    requestEntity
                )
            if (newRequestEntity == null) {
                return@withContext Response(
                    requestEntity,
                    Response.RESPONSE_CODE_REQUEST_INTERCEPTED,
                    emptyMap(),
                    null,
                    requestTime,
                    System.currentTimeMillis(),
                    null
                )
            }
            val tag = "${System.currentTimeMillis()}-${newRequestEntity.hashCode()}-${Thread.currentThread().id}"
            if (newRequestEntity.timeout != config.timeout) {
                requestTimeoutProperties[tag] = newRequestEntity.timeout
            }

            val resp = try {
                httpClient.newCall(
                    Request.Builder()
                        .tag(tag)
                        .url(getFullUrl(newRequestEntity.url, newRequestEntity.method, newRequestEntity.queries, newRequestEntity.params))
                        .apply {
                            newRequestEntity.headers.forEach { (t, u) ->
                                addHeader(t, u.toString())
                            }
                        }
                        .method(
                            newRequestEntity.method,
                            when (newRequestEntity.method) {
                                "GET", "HEAD" -> null
                                else -> {
                                    if (newRequestEntity.body != null) {
                                        newRequestEntity.body.let { body ->
                                            body.data.toRequestBody(body.contentType.toMediaTypeOrNull())
                                        }
                                    } else {
                                        FormBody.Builder().apply {
                                            newRequestEntity.params.forEach { (t, u) ->
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
                    Response(newRequestEntity, it.code, headers.toMap(), content.let { byteArray ->
                        if (byteArray == null) {
                            null
                        } else {
                            val contentType = (it.body?.contentType()?.toString() ?: headers["content-type"]
                            ?: "text/plain;charset=UTF-8").lowercase()
                            val charset = try {
                                val startIndex = contentType.indexOf("charset=")
                                val charsetStr = if (startIndex != -1) {
                                    val endIndex = contentType.indexOf(";", startIndex + 8)
                                    contentType.substring(
                                        startIndex + 8,
                                        if (endIndex != -1) endIndex else contentType.length
                                    ).uppercase()
                                } else {
                                    "UTF-8"
                                }
                                Charset.forName(charsetStr)
                            } catch (ignore: Throwable) {
                                defaultCharset
                            }
                            if (isTextBody(contentType)) {
                                Response.Body(
                                    data = byteArray,
                                    dataStr = String(byteArray, charset),
                                    charset = charset.name()
                                )
                            } else {
                                Response.Body(
                                    data = byteArray,
                                    dataStr = "",
                                    charset = charset.name()
                                )
                            }

                        }
                    }, requestTime, System.currentTimeMillis())
                }
            } catch (tr: Throwable) {
                Response(
                    newRequestEntity,
                    Response.RESPONSE_CODE_THROW_EXCEPTION,
                    emptyMap(),
                    null,
                    requestTime,
                    System.currentTimeMillis(),
                    tr
                )
            } finally {
                requestTimeoutProperties.remove(tag)
            }
            val blocked = if (!newRequestEntity.hasProhibit(RequestEntity.PROHIBIT_FLAG_RESPONSE_INTERCEPTOR)) {
                config.responseInterceptor(NotCallerThread, resp)
            } else {
                false
            }
            if (!blocked) {
                resp
            } else {
                Response(
                    newRequestEntity,
                    Response.RESPONSE_CODE_BLOCKED,
                    emptyMap(),
                    null,
                    requestTime,
                    System.currentTimeMillis(),
                    null
                )
            }
        }
    }

    fun enqueue(
        requestEntity: RequestEntity,
        response: NotCallerThread.(Response) -> Unit
    ) = requesterGlobalScope.launch(getDispatcher()) {
        response(NotCallerThread, call(requestEntity))
    }


    private fun getFullUrl(url: String, method:String, queries: Map<String, Any> ,params:Map<String, Any>): String {
        val sb = StringBuilder()
        if (!url.startsWith("https://", true) && !url.startsWith("http://", true)) {
            sb.append(config.baseUrl)
            sb.append("/")
            sb.append(url)
        } else {
            sb.append(url)
        }
        var hasQuery = sb.contains("?")
        fun appendMap(map:Map<String, Any>) {
            map.forEach { (t, u) ->
                if(!hasQuery) {
                    sb.append("?")
                    hasQuery = true
                }  else {
                    sb.append("&")
                }
                sb.append(t).append("=").append(URLEncoder.encode(u.toString(), "UTF-8"))
            }
        }
        appendMap(queries)
        if(params.isNotEmpty() && (method == "GET" || method == "HEADER") ) {
            appendMap(params)
        }
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