package com.llnhhy.requester3.response

import com.llnhhy.requester3.request.RequestEntity

class Response(
    val requestInfo: RequestEntity,
    val code: Int = 0,
    val headers: Map<String, String>,
    val body: Body?,
    val requestTime:Long,
    val responseTime:Long,
    val throwable: Throwable? = null
) {

    companion object {
        const val RESPONSE_CODE_THROW_EXCEPTION = -1
        const val RESPONSE_CODE_REQUEST_INTERCEPTED = -2
        const val RESPONSE_CODE_BLOCKED = -3
    }

    class Body(val data: ByteArray, val dataStr: String, val charset: String) {
        override fun toString(): String {
            return "Body(byteArrayLength=${data.size}, dataStr='${dataStr}', charset='$charset')"
        }
    }

    override fun toString(): String {
        return "Response(requestInfo=$requestInfo, code=$code, headers=$headers, body=$body, requestTime=$requestTime, responseTime=$responseTime, throwable=$throwable)"
    }


}