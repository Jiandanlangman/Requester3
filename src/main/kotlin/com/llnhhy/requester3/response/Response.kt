package com.llnhhy.requester3.response

import com.llnhhy.requester3.negotiation.JsonDecoder
import com.llnhhy.requester3.negotiation.TransformInfo
import com.llnhhy.requester3.request.RequestEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.serializer
import java.nio.charset.Charset
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class Response(
    val requestInfo: RequestEntity,
    val code: Int = 0,
    val headers: Map<String, String>,
    val body: ByteArray?,
    val requestTime: Long,
    val responseTime: Long,
    val throwable: Throwable? = null
) {

    companion object {

        const val CODE_REQUEST_ERROR = -1
        const val CODE_REQUEST_CANCELED = -2

        private var jsonDecoder : JsonDecoder = json {
            explicitNulls = false
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }

        infix fun contentNegotiation(jsonDecoder: JsonDecoder) {
            this.jsonDecoder = jsonDecoder
        }

        infix fun json(builder: JsonBuilder.() -> Unit) = object : JsonDecoder {

            private val json = Json { apply(builder) }

            override fun <T> decodeFromBody(type: KType, body: ByteArray, charset: Charset): T? {
                if (type == typeOf<String>()) {
                    @Suppress("UNCHECKED_CAST")
                    return String(body, charset) as T
                }
                try {
                    @Suppress("UNCHECKED_CAST")
                    return json.decodeFromString<T>(deserializer = serializer(type) as KSerializer<T>, string = String(body, charset))
                } catch (tr: Throwable) {
                    tr.printStackTrace()
                }
                return null
            }
        }


    }

    val contentType: String
        get() = headers["content-type"] ?: "text/plain;charset=UTF-8"

    val charset: Charset
        get() = try {
            val startIndex = contentType.indexOf("charset=")
            val charsetStr = if (startIndex != -1) {
                val endIndex = contentType.indexOf(";", startIndex + 8)
                contentType.substring(
                    startIndex + 8,
                    if (endIndex != -1) endIndex else contentType.length
                ).uppercase()
            } else {
                "UTF-8"
            }
            Charset.forName(charsetStr)
        } catch (ignore: Throwable) {
            Charset.forName("UTF-8")
        }

    fun <T> bodyTransformTo(type: KType) : T? = body?.let {
        jsonDecoder.decodeFromBody<T>(type, it, charset)
    }

    inline fun <reified T> body() = bodyTransformTo<T>(typeOf<T>())

    inline infix fun <reified T> bodyTransformTo(info : TransformInfo<T>) : T? {
        return bodyTransformTo(typeOf<T>())
    }


    override fun toString(): String {
        return "Response(urrequestInfo=$requestInfo, code=$code, headers=$headers, body=${if (body == null) "null" else "'${String(body, charset)}'"}, requestTime=$requestTime, responseTime=$responseTime)"
    }
}

