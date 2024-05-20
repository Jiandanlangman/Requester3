package com.llnhhy.requester3.request

import com.llnhhy.requester3.config.Timeout
import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.NotCallerThread


@DslMarker
annotation class RequesterDSLMarker

@RequesterDSLMarker
interface RequesterDSLScope

object call : RequesterDSLScope

abstract class MethodDSL internal constructor(val name: String)

object HttpGet : MethodDSL("GET")

object HttpPost : MethodDSL("POST")

object HttpPut : MethodDSL("PUT")

object HttpPatch : MethodDSL("PATCH")

object HttpDelete : MethodDSL("DELETE")

object HttpHead : MethodDSL("HEAD")

abstract class CallOrEnqueueScope internal constructor(internal val builder: RequestBuilder) : RequesterDSLScope

class RequestBuilderScope internal constructor(builder: RequestBuilder) : CallOrEnqueueScope(builder)

abstract class AbsReqBodyScope(internal val builder: RequestBuilder) : RequesterDSLScope

class WhereScope internal constructor(builder: RequestBuilder) : AbsReqBodyScope(builder)

class BodyContentType internal constructor(internal val contentType: String)

class BodyScope internal constructor(
    builder: RequestBuilder,
    var data: ByteArray = byteArrayOf()
) : AbsReqBodyScope(builder) {
    fun contentType(getter:() -> String) =  BodyContentType(getter())
}


class TimeoutScope internal constructor(
    var collect: Int = Timeout.Default.connect,
    var write: Int = Timeout.Default.write,
    var read: Int = Timeout.Default.read
) : RequesterDSLScope

infix fun <T, K> K?.fromNotNullGet(block: K.() -> T) : T? {
    if(this == null) {
        return null
    }
    return block()
}

infix fun <T> T?.blocking(block: T?.() -> Unit): T? {
    block()
    return this
}

infix fun <T> T?.blockingNotNull(block: T.() -> Unit): T? {
    if(this != null) {
        block()
    }
    return this
}

infix fun <T> T?.defaultValue(value:T) : T {
    if(this != null) {
        return this
    }
    return value
}


infix fun MethodDSL.from(url: String): RequestBuilderScope {

    return RequestBuilderScope(RequestBuilder.newBuilder().url(url).apply {
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


suspend infix fun CallOrEnqueueScope.sync(c: call) = builder.build().call()

infix fun CallOrEnqueueScope.enqueue(response:NotCallerThread.(Response) -> Unit ) = builder.build().enqueue(response)


infix fun RequestBuilderScope.where(block: WhereScope.() -> Unit): CallOrEnqueueScope {
    WhereScope(builder).apply(block)
    return this
}

infix fun RequestBuilderScope.body(block: BodyScope.() -> BodyContentType): CallOrEnqueueScope {
    val scope = BodyScope(builder)
    val contentType = block(scope).contentType
    builder.body(Body(contentType, scope.data))
    return this
}

infix fun RequestBuilderScope.prohibit(prohibit: () -> Int) = apply {
    builder.prohibit(prohibit())
}


infix fun RequestBuilderScope.timeout(timeoutBuilder: TimeoutScope.() -> Unit) = apply {
    val t = TimeoutScope().apply(timeoutBuilder)
    builder.timeout(Timeout(connect = t.collect, write = t.write, read = t.read))
}

infix fun AbsReqBodyScope.header(block: () -> Pair<String, Any?>) = apply {
    val header = block()
    builder.appendHeader(header.first, header.second)
}

infix fun AbsReqBodyScope.query(block: () -> Pair<String, Any?>) = apply {
    val query = block()
    builder.appendQuery(query.first, query.second)
}


infix fun WhereScope.param(block: () -> Pair<String, Any?>) = apply {
    val param = block()
    builder.appendParam(param.first, param.second)
}