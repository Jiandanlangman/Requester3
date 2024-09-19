package com.llnhhy.requester3.request

import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.NotCallerThread
import com.llnhhy.requester3.Requester
import com.llnhhy.requester3.config.Timeout
import kotlinx.coroutines.Job


@DslMarker
annotation class RequesterDSLMarker

@RequesterDSLMarker
interface RequesterDSLScope

interface CallOrEnqueueScope : RequesterDSLScope {

    suspend infix fun call(sync: sync): Response

    infix fun enqueue(response: NotCallerThread.(Response) -> Unit): Job
}

interface FormDataScope : CallOrEnqueueScope {
    infix fun formData(builder: MutableMapBuilder.() -> Unit): CallOrEnqueueScope
}

interface BodyDataScope : CallOrEnqueueScope {
    infix fun body(builder: BodyBuilder.() -> BodyContentType): CallOrEnqueueScope
}


interface QueriesScope : CallOrEnqueueScope {

    infix fun queries(builder: MutableMapBuilder.() -> Unit): HeadersAndTimeoutScope

}

interface HeadersScope : CallOrEnqueueScope {
    infix fun headers(builder: MutableMapBuilder.() -> Unit): QueriesAndTimeoutScope
}

interface TimeoutScope : CallOrEnqueueScope {
    infix fun timeout(builder: TimeoutBuilder.() -> Unit): QueriesAndHeadersScope
}

interface HeadersAndTimeoutScope : HeadersScope, TimeoutScope, FormDataScope, BodyDataScope

interface QueriesAndTimeoutScope : QueriesScope, TimeoutScope, FormDataScope, BodyDataScope

interface QueriesAndHeadersScope : QueriesScope, HeadersScope, FormDataScope, BodyDataScope


object sync : RequesterDSLScope

class RequestBuilderScope internal constructor(private val requestBuilder: RequestBuilder) : HeadersAndTimeoutScope, QueriesAndTimeoutScope, QueriesAndHeadersScope {


    override suspend fun call(sync: sync) = Requester.call(requestBuilder.build())


    override fun enqueue(response: NotCallerThread.(Response) -> Unit) = Requester.enqueue(requestBuilder.build(), response)


    override fun timeout(builder: TimeoutBuilder.() -> Unit): QueriesAndHeadersScope = apply {
        val timeOutBuilder = TimeoutBuilder().apply(builder)
        requestBuilder.timeout(Timeout(connect = timeOutBuilder.collect, write = timeOutBuilder.write, read = timeOutBuilder.read))

    }

    override fun queries(builder: MutableMapBuilder.() -> Unit): HeadersAndTimeoutScope = apply {
        requestBuilder.queries {
            MutableMapBuilder(this).apply(builder)
        }
    }

    override fun headers(builder: MutableMapBuilder.() -> Unit): QueriesAndTimeoutScope = apply {
        requestBuilder.headers {
            MutableMapBuilder(this).apply(builder)
        }
    }

    override fun body(builder: BodyBuilder.() -> BodyContentType): CallOrEnqueueScope = apply {
        val bodyBuilder = BodyBuilder()
        val contentType = builder.invoke(bodyBuilder)
        requestBuilder.body(Body(data = bodyBuilder.data, contentType = contentType.toString()))
    }


    override fun formData(builder: MutableMapBuilder.() -> Unit): CallOrEnqueueScope = apply {
        requestBuilder.formData {
            MutableMapBuilder(this).apply(builder)
        }
    }


}


abstract class MethodDSL internal constructor(private val name: String) {

    infix fun from(url: String): RequestBuilderScope {
        return RequestBuilderScope(RequestBuilder.newBuilder().url(url)
            .apply {
                when (name) {
                    "POST" -> post()
                    "PUT" -> put()
                    "PATCH" -> patch()
                    "DELETE" -> delete()
                    "HEAD" -> head()
                    else -> get()
                }
            })
    }
}


object HttpGet : MethodDSL("GET")

object HttpPost : MethodDSL("POST")

object HttpPut : MethodDSL("PUT")

object HttpPatch : MethodDSL("PATCH")

object HttpDelete : MethodDSL("DELETE")

object HttpHead : MethodDSL("HEAD")


class MutableMapBuilder internal constructor(private val map: MutableMap<String, Any>) {

    infix fun put(block: () -> Pair<String, Any>) {
        block().let {
            map[it.first] = it.second
        }
    }

    infix fun remove(block: () -> String) {
        map.remove(block())
    }

}

class TimeoutBuilder internal constructor(
    var collect: Int = Timeout.Default.connect,
    var write: Int = Timeout.Default.write,
    var read: Int = Timeout.Default.read
) : RequesterDSLScope


class BodyContentType internal constructor(private val contentType: String) {
    override fun toString() = contentType
}

class BodyBuilder internal constructor(
    var data: ByteArray = byteArrayOf()
) {
    fun contentType(block: () -> String) = BodyContentType(block())
}

infix fun <T, K> K?.fromNotNullGet(block: K.() -> T): T? {
    if (this == null) {
        return null
    }
    return block()
}

infix fun <T> T?.blocking(block: T?.() -> Unit): T? {
    block()
    return this
}

infix fun <T> T?.blockingNotNull(block: T.() -> Unit): T? {
    if (this != null) {
        block()
    }
    return this
}

infix fun <T> T?.defaultValue(value: T): T {
    if (this != null) {
        return this
    }
    return value
}