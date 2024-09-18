package com.llnhhy.requester3.request

import com.llnhhy.requester3.Requester
import com.llnhhy.requester3.config.Timeout

class RequestBuilder private constructor(entity: RequestEntity?) {

    companion object {

        private  val urlRegex = Regex("^/+")

        fun newBuilder() = RequestBuilder(null)

        fun newBuilder(info: RequestEntity) = RequestBuilder(info)

    }


    private class MutableRequestEntity(
        var url: String,
        var method: String,
        var headers: MutableMap<String, Any>,
        var params: MutableMap<String, Any>,
        val queries:MutableMap<String, Any>,
        var body: Body?,
        var timeout: Timeout,
        var prohibit: Int
    ) {

        fun toRequestEntity() = RequestEntity(url, method, headers, params, queries, body, timeout, prohibit)

        companion object {

            fun newInstance(): MutableRequestEntity {
                val defaultConfig = Requester.config
                return MutableRequestEntity("", defaultConfig.method, mutableMapOf(), mutableMapOf(), mutableMapOf(), null, defaultConfig.timeout, 0)
            }

            fun newInstance(entity: RequestEntity): MutableRequestEntity {
                return MutableRequestEntity(
                    entity.url,
                    entity.method,
                    entity.headers.toMutableMap(),
                    entity.params.toMutableMap(),
                    entity.queries.toMutableMap(),
                    entity.body,
                    entity.timeout,
                    entity.prohibit
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

    fun appendHeader(key: String, value: Any?) = apply {
        mutableRequestEntity.headers.let {
            if (value == null) {
                it.remove(key)
            } else {
                it[key] = value
            }
        }
    }

    fun appendParam(key: String, value: Any?) = apply {
        mutableRequestEntity.params.let {
            if (value == null) {
                it.remove(key)
            } else {
                it[key] = value
            }
        }
    }

    fun appendQuery(key: String, value: Any?) = apply {
        mutableRequestEntity.queries.let {
            if (value == null) {
                it.remove(key)
            } else {
                it[key] = value
            }
        }
    }

    fun body(body: Body) = apply {
        mutableRequestEntity.body = body
    }

    fun timeout(timeout: Timeout) = apply {
        mutableRequestEntity.timeout = timeout
    }

    fun prohibit(prohibit: Int) = apply {
        mutableRequestEntity.prohibit = prohibit
    }


    fun appendHeaders(headers: Map<String, Any?>) = apply {
        headers.forEach { (t, u) ->
            appendHeader(t, u)
        }
    }

    fun appendParams(params: Map<String, Any?>) = apply {
        params.forEach { (t, u) ->
            appendParam(t, u)
        }
    }

    fun appendQueries(queries: Map<String, Any?>) = apply {
        queries.forEach { (t, u) ->
            appendQuery(t, u)
        }
    }

}