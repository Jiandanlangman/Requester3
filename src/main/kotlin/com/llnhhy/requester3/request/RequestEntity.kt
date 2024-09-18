package com.llnhhy.requester3.request

import com.llnhhy.requester3.Requester
import com.llnhhy.requester3.config.Timeout
import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.NotCallerThread




/**
 * body
 */
class RequestEntity internal constructor(
    val url: String,
    val method: String,
    val headers: Map<String, Any>,
    val params: Map<String, Any>,
    val queries:Map<String, Any>,
    val body: Body?,
    val timeout: Timeout,
    val prohibit: Int
) {

    companion object {
        const val PROHIBIT_FLAG_PROCESSOR = 1
        const val PROHIBIT_FLAG_RESPONSE_INTERCEPTOR = 2
    }


    fun hasProhibit(prohibit: Int): Boolean {
        return (this.prohibit and prohibit) == prohibit
    }

    suspend fun call() = Requester.call(this)

    fun enqueue(response: NotCallerThread.(Response) -> Unit) = Requester.enqueue(requestEntity = this, response = response)

    fun newBuilder() = RequestBuilder.newBuilder(this)


    override fun toString(): String {
        return "RequestEntity(url='$url', method='$method', headers=$headers, params=$params, body=$body, timeout=$timeout, prohibit=$prohibit)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestEntity

        if (url != other.url) return false
        if (method != other.method) return false
        if (headers != other.headers) return false
        if (params != other.params) return false
        if (body != other.body) return false
        if (timeout != other.timeout) return false
        if (prohibit != other.prohibit) return false
        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + timeout.hashCode()
        result = 31 * result + prohibit
        return result
    }


}