package com.llnhhy.requester3.negotiation

import com.llnhhy.requester3.response.Response
import java.nio.charset.Charset
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface JsonDecoder {

    fun <T> decodeFromBody(type: KType, body: ByteArray, charset: Charset): T?

}

inline fun <reified T : Any> typeOf() = TransformInfo(typeOf<T>(), T::class)


class TransformInfo<T : Any>(internal val type: KType, clazz: KClass<T>)



