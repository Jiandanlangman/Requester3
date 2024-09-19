package com.llnhhy.requester3.request

import com.llnhhy.requester3.config.Timeout


/**
 * body
 */
class RequestEntity internal constructor(
    val url: String,
    val method: String,
    val headers: Map<String, Any>,
    val formData: Map<String, Any>,
    val queries: Map<String, Any>,
    val body: Body?,
    val timeout: Timeout
) {

    fun newBuilder() = RequestBuilder.newBuilder(this)

    override fun toString(): String {
        return "RequestEntity(url='$url', method='$method', queries=$queries, headers=$headers, formData=$formData, body=$body, timeout=$timeout)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestEntity

        if (url != other.url) return false
        if (method != other.method) return false
        if (headers != other.headers) return false
        if (formData != other.formData) return false
        if (body != other.body) return false
        if (timeout != other.timeout) return false
        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + formData.hashCode()
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + timeout.hashCode()
        return result
    }


}