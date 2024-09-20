package com.llnhhy.requester3.negotiation

import java.nio.charset.Charset
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface JsonDecoder {

    fun <T> decodeFromBody(type: KType, body: ByteArray, charset: Charset): T?

}

class TransformInfo<T >(internal val type: KType)

inline fun <reified T > typeOf() = TransformInfo<T>(typeOf<T>())




