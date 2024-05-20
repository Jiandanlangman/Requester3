package com.llnhhy.requester3.transformation

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.llnhhy.requester3.response.Response


object GsonTransform {

    var gson: Gson = GsonBuilder().create()
        private set
    var exceptionHandler:((Throwable) -> Unit)?= null
        private set

    fun setGsonInstance(instance: Gson) {
        gson = instance
    }

    fun exceptionHandler(handler:(Throwable) -> Unit) {
        exceptionHandler = handler
    }
}


inline fun <reified T> typeOf() = object : TypeToken<T>() {}


fun <T> Response?.transform(type: TypeToken<T>, onException:((Throwable?) -> Unit)? = null ) : T? {
    val content = this?.body?.dataStr ?: return null
    if(content.isEmpty()) {
        return null
    }
    if(isBasicType(type)) {
        return content as T
    }
    return try {
        GsonTransform.gson.fromJson(content, type)
    } catch (tr:Throwable) {
        onException?.invoke(tr) ?: GsonTransform.exceptionHandler?.invoke(tr)
        null
    }
}


private fun isBasicType(type: TypeToken<*>) : Boolean {
    println("name:${type.rawType.name}")
    when(type.rawType.name) {
        "java.lang.String",
            "java.lang.Integer",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Character",
            "java.lang.Boolean",
            "java.lang.Byte" -> {
                return true
            }
    }
   return false
}


//DSL

infix fun < T> Response?.transformTo(type: TypeToken<T>)  = transform(type)


inline infix fun <reified T> Response?.transformTo(ignore: Class<T>) = transformTo<T>(typeOf())


