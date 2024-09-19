package com.llnhhy.requester3

import com.llnhhy.requester3.config.ConfigBuilder
import com.llnhhy.requester3.negotiation.*
import com.llnhhy.requester3.request.*
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import sun.rmi.runtime.Log
import java.nio.charset.Charset
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger


//fun newCachedThreadPoolCoroutineDispatcher(coreSize: Int, name: String): ExecutorCoroutineDispatcher {
//    val threadNo = AtomicInteger()
//
//    return ThreadPoolExecutor(
//        coreSize, coreSize * 2,
//        10L, TimeUnit.SECONDS,
//        LinkedBlockingDeque(),
//        ThreadFactory {
//            Thread(it, "CoilCustomFetcherDispatcher-${threadNo.incrementAndGet()}").apply {
//                isDaemon = true
//            }
//        }
//    ).asCoroutineDispatcher()
//}

fun main() {


//    testSer()
    testRequest()
}

private fun testRequest() {
    Requester config {
        interceptor {
            println("interceptor:${it.url}")
            proceed(it.newBuilder().url("https://www.ithome.com/").queries {
                put("name", 123)
            }.headers {
                put("dasda", 222)
            }.build())
        }
    }
    runBlocking {
      val response =  HttpGet from "https://www.baidu.com/" call sync
        println(response)
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalStdlibApi::class)
fun testSer() {
    val data = """
        [
        {"id":1, "name":"abc", "labels":[{"icon":"dadsadas", "name":"label1", "sort":0},{"icon":"dadsadas", "name":"label2", "sort":1}]},
        {"id":2, "name":"fggg", "labels":[{"icon":"dadsadas", "name":"label1", "sort":0},{"icon":"dadsadas", "name":"label2", "sort":1}]},
        {"id":3, "name":"srtg", "labels":[{"icon":"dadsadas", "name":"label1", "sort":0},{"icon":"dadsadas", "name":"label2", "sort":1}]}
        ]
    """.trimIndent()
    val response = Response(
        requestInfo = RequestBuilder.newBuilder().build(),
        code = 0,
        headers = mapOf("content-type" to "UTF-8"),
        body = data.toByteArray(),
        requestTime = 0L,
        responseTime = 0L
    )
    val resp = response bodyTransformTo typeOf<List<TestModel>>()
    println(resp)
}

@Serializable
data class TestModel(
    val id:Int,
    val name:String,
    val labels:List<Label>
)

@Serializable
data class Label(val icon:String, val name:String, val sort:Int)

