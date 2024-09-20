package com.llnhhy.requester3.request

import com.llnhhy.requester3.Requester
import com.llnhhy.requester3.config.Timeout

class RequestBuilder private constructor(entity: RequestEntity?) {

    companion object {

        private val urlRegex = Regex("^/+")

        fun newBuilder() = RequestBuilder(null)

        fun newBuilder(info: RequestEntity) = RequestBuilder(info)

    }


    private class MutableRequestEntity(
        var url: String,
        var method: String,
        var headers: MutableMap<String, Any>,
        var formData: MutableMap<String, Any>,
        val queries: MutableMap<String, Any>,
        var body: Body?,
        var timeout: Timeout
    ) {

        fun toRequestEntity() = RequestEntity(url.let {
            val sb = StringBuilder()
            if (!it.startsWith("https://", true) && !it.startsWith("http://", true)) {
                sb.append(Requester.config.baseUrl)
                sb.append("/")
            }
            sb.append(it)
            sb.toString()
        }, method, headers, formData, queries, body, timeout)

        companion object {

            fun newInstance(): MutableRequestEntity {
                val defaultConfig = Requester.config
                return MutableRequestEntity("", defaultConfig.defaultMethod, mutableMapOf(), mutableMapOf(), mutableMapOf(), null, defaultConfig.timeout)
            }

            fun newInstance(entity: RequestEntity): MutableRequestEntity {
                return MutableRequestEntity(
                    entity.url,
                    entity.method,
                    entity.headers.toMutableMap(),
                    entity.formData.toMutableMap(),
                    entity.queries.toMutableMap(),
                    entity.body,
                    entity.timeout
                )
            }

        }

    }


    private val mutableRequestEntity =
        if (entity != null) MutableRequestEntity.newInstance(entity) else MutableRequestEntity.newInstance()


    fun build(): RequestEntity {
        return mutableRequestEntity.toRequestEntity()
    }


    fun url(url: String) = apply {
        mutableRequestEntity.url = url.replace(urlRegex, "")
    }


    fun get() = apply {
        mutableRequestEntity.method = "GET"
    }

    fun head() = apply {
        mutableRequestEntity.method = "HEAD"
    }

    fun post() = apply {
        mutableRequestEntity.method = "POST"
    }

    fun put() = apply {
        mutableRequestEntity.method = "PUT"
    }

    fun patch() = apply {
        mutableRequestEntity.method = "PATCH"
    }

    fun delete() = apply {
        mutableRequestEntity.method = "DELETE"
    }

    fun method(method: String) = apply {
        mutableRequestEntity.method = method
    }

    fun headers(headers: MutableMap<String, Any>.() -> Unit) = apply {
        mutableRequestEntity.headers.apply(headers)
    }

    fun formData(formData: MutableMap<String, Any>.() -> Unit) = apply {
        mutableRequestEntity.formData.apply(formData)
    }

    fun queries(queries: MutableMap<String, Any>.() -> Unit) = apply {
        mutableRequestEntity.queries.apply(queries)
    }


    fun body(body: Body) = apply {
        mutableRequestEntity.body = body
    }

    fun timeout(timeout: Timeout) = apply {
        mutableRequestEntity.timeout = timeout
    }


}