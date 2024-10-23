package com.llnhhy.requester3.core

import com.llnhhy.requester3.DNS
import com.llnhhy.requester3.config.Timeout
import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.collections.get

internal class OKHttpCore(
    sslSocketFactory: SSLSocketFactory,
    trustManager: X509TrustManager,
    hostnameVerifier: HostnameVerifier,
    dns: DNS
) : RequesterCore {

    private val requestTimeoutProperties = HashMap<String, Timeout>()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor {
            val timeOut = requestTimeoutProperties[it.request().tag()]
            if (timeOut != null) {
                it.withReadTimeout(timeOut.read, TimeUnit.MILLISECONDS)
                    .withConnectTimeout(timeOut.connect, TimeUnit.MILLISECONDS)
                    .withWriteTimeout(timeOut.write, TimeUnit.MILLISECONDS)
                    .proceed(it.request())
            } else {
                it.proceed(it.request())
            }

        }
        .sslSocketFactory(sslSocketFactory, trustManager)
        .hostnameVerifier(hostnameVerifier)
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> = dns.lookup(hostname)

        })
        .build()

    override suspend fun call(entity: RequestEntity): Response = withContext(Dispatchers.IO) {//TODO dispatcher....
        val requestTime = System.currentTimeMillis()
        val tag = "${System.currentTimeMillis()}-${entity.hashCode()}-${Thread.currentThread().id}"
        requestTimeoutProperties[tag] = entity.timeout
        try {
            httpClient.newCall(
                Request.Builder()
                    .tag(tag)
                    .url(getFullUrl(entity.url, entity.queries))
                    .apply {
                        entity.headers.forEach { (t, u) ->
                            addHeader(t, u.toString())
                        }
                    }
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


    private fun getFullUrl(url: String, queries: Map<String, Any>): String {
        val sb = StringBuilder()
        sb.append(url)
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
        return sb.toString()
    }

    fun close() {
        httpClient.cache?.close()
        httpClient.connectionPool.evictAll()
        httpClient.dispatcher.executorService.shutdown()
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

}