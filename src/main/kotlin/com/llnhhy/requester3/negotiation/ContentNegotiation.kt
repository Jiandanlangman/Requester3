package com.llnhhy.requester3.negotiation

import com.llnhhy.requester3.response.Response
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.serializer
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface JsonDecoder {

    fun <T> decodeFromString(type: KType, body: Response.Body): T?

}

class KJsonDecoder(private val json: Json) : JsonDecoder {
    override fun <T> decodeFromString(type: KType, body: Response.Body): T? {
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

    infix fun kJson(jsonBuilder: JsonBuilder.() -> Unit) {
        innerJsonDecoder = KJsonDecoder(Json { this.apply(jsonBuilder) })
    }

    infix fun custom(decoder: JsonDecoder) {
        innerJsonDecoder = decoder
    }

}

infix fun <T> Response.body(KTypeInfo: KTypeInfo<T>) = body?.let { jsonDecoder.decodeFromString<T>(KTypeInfo.type, it) }


inline fun <reified T> transformTo(): KTypeInfo<T> {
    return KTypeInfo(typeOf<T>(), T::class)
}

class KTypeInfo<T>(val type: KType, clazz: KClass<*>)