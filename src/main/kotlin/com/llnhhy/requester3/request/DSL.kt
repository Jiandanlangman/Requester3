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

interface BodyScope : CallOrEnqueueScope {
    infix fun body(builder: BodyBuilder.() -> BodyContentType): CallOrEnqueueScope
}

object sync : RequesterDSLScope


interface A {
    infix fun timeout(t:Int) : A
}

open class B : A {
    override fun timeout(t: Int): B {
        return this
    }

    fun b() {

    }

}

class C : B() {
    override fun timeout(t: Int): C {
        super.timeout(t)
        return this
    }

    fun c() {

    }
}

fun test() {

}

//interface PropertiesScope {
//    infix fun timeout(builder: TimeoutBuilder.() -> Unit) : PropertiesScope
//    infix fun queries(builder: MutableMapBuilder.() -> Unit): PropertiesScope
//    infix fun
//}

open class NoBodyRequesterBuilderScope internal constructor(private val requestBuilder: RequestBuilder) : CallOrEnqueueScope {

    override suspend fun call(sync: sync) = Requester.call(requestBuilder.build())

    override fun enqueue(response: NotCallerThread.(Response) -> Unit) = Requester.enqueue(requestBuilder.build(), response)

    open infix fun timeout(builder: TimeoutBuilder.() -> Unit) = apply {
        val timeOutBuilder = TimeoutBuilder().apply(builder)
        requestBuilder.timeout(Timeout(connect = timeOutBuilder.collect, write = timeOutBuilder.write, read = timeOutBuilder.read))
    }

    open infix fun queries(builder: MutableMapBuilder.() -> Unit) = apply {
        requestBuilder.queries {
            MutableMapBuilder(this).apply(builder)
        }
    }

    open infix fun headers(builder: MutableMapBuilder.() -> Unit) = apply {
        requestBuilder.headers {
            MutableMapBuilder(this).apply(builder)
        }
    }

}

class RequestBuilderScope internal constructor(private val requestBuilder: RequestBuilder) : NoBodyRequesterBuilderScope(requestBuilder), FormDataScope, BodyScope  {

    override fun timeout(builder: TimeoutBuilder.() -> Unit): RequestBuilderScope = apply {
        super.timeout(builder)
    }

    override fun queries(builder: MutableMapBuilder.() -> Unit): RequestBuilderScope = apply {
        super.queries(builder)
    }

    override fun headers(builder: MutableMapBuilder.() -> Unit): RequestBuilderScope = apply {
        super.headers(builder)
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

abstract class NoBodyRequestInfixStart internal constructor(private val method: String) {
    infix fun from(url: String) : NoBodyRequesterBuilderScope {
        return NoBodyRequesterBuilderScope(RequestBuilder.newBuilder().url(url).apply {
            when(method) {
                "HEAD" -> head()
                else -> get()
            }
        })
    }
}

abstract class RequestInfixStart internal constructor(private val name: String) {

    infix fun from(url: String): RequestBuilderScope {
        return RequestBuilderScope(RequestBuilder.newBuilder().url(url)
            .apply {
                when (name) {
                    "PUT" -> put()
                    "PATCH" -> patch()
                    "DELETE" -> delete()
                    else -> post()
                }
            })
    }
}


object HttpGet : NoBodyRequestInfixStart("GET")

object HttpPost : RequestInfixStart("POST")

object HttpPut : RequestInfixStart("PUT")

object HttpPatch : RequestInfixStart("PATCH")

object HttpDelete : RequestInfixStart("DELETE")

object HttpHead : NoBodyRequestInfixStart("HEAD")


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