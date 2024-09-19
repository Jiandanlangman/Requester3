package com.llnhhy.requester3.response

import com.llnhhy.requester3.request.RequestEntity
import java.nio.charset.Charset

class Response(
    val requestInfo: RequestEntity,
    val code: Int = 0,
    val headers: Map<String, String>,
    val body: ByteArray?,
    val requestTime: Long,
    val responseTime: Long,
    val throwable: Throwable? = null
) {

    companion object {
        const val CODE_REQUEST_ERROR = -1
        const val CODE_REQUEST_CANCELED = -2
    }

    val contentType: String
        get() = headers["content-type"] ?: "text/plain;charset=UTF-8"

    val charset: Charset
        get() = try {
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
            Charset.forName("UTF-8")
        }

    override fun toString(): String {
        return "Response(urrequestInfo=$requestInfo, code=$code, headers=$headers, body=${if(body == null) "null" else "'${String(body, charset)}'"}, requestTime=$requestTime, responseTime=$responseTime)"
    }
}