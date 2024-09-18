package com.llnhhy.requester3.negotiation

import com.llnhhy.requester3.response.Response
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.serializer
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.KType


interface JsonDecoder {

    fun <T> decodeFromBody(type: KType, body: Response.Body): T?

}

private class KJsonDecoder(private val json: Json) : JsonDecoder {

    override fun <T> decodeFromBody(type: KType, body: Response.Body): T? {
        try {
            @Suppress("UNCHECKED_CAST")
            return json.decodeFromString<T>(deserializer = serializer(type) as KSerializer<T>, string = String(bytes = body.data, Charset.forName(body.charset)))
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        return null
    }

}


private var innerJsonDecoder: JsonDecoder = KJsonDecoder(Json {
    explicitNulls = false
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
})

val jsonDecoder: JsonDecoder
    get() = innerJsonDecoder


object ResponseContentNegotiation {

    infix fun json(jsonBuilder: JsonBuilder.() -> Unit) {
        innerJsonDecoder = KJsonDecoder(Json { this.apply(jsonBuilder) })
    }

    infix fun custom(decoder: JsonDecoder) {
        innerJsonDecoder = decoder
    }

}

infix fun <T : Any> Response.bodyTransformTo(typeInfo: TransformInfo<T>) = body?.let { jsonDecoder.decodeFromBody<T>(typeInfo.type, it) }

inline fun <reified T : Any> typeOf() = TransformInfo(kotlin.reflect.typeOf<T>(), T::class)


class TransformInfo<T : Any>(internal val type: KType, clazz: KClass<T>)