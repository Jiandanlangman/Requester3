package com.llnhhy.requester3

import com.llnhhy.requester3.config.ConfigBuilder
import com.llnhhy.requester3.negotiation.*
import com.llnhhy.requester3.request.*
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.nio.charset.Charset
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap


fun newCachedThreadPoolCoroutineDispatcher(coreSize: Int, name: String): ExecutorCoroutineDispatcher {
    val threadNo = AtomicInteger()

    return ThreadPoolExecutor(
        coreSize, coreSize * 2,
        10L, TimeUnit.SECONDS,
        LinkedBlockingDeque(),
        ThreadFactory {
            Thread(it, "CoilCustomFetcherDispatcher-${threadNo.incrementAndGet()}").apply {
                isDaemon = true
            }
        }
    ).asCoroutineDispatcher()
}

fun main() {

//    runBlocking {
//        val user = RequestBuilder.newBuilder()
//            .url("user/info")
//            .get()
//            .appendParam("id", 12345)
//            .build()
//            .call()
//            .transformTo(typeOf<User>())
//    }


//    val a = ThreadPoolExecutor(
//        8, 10,
//        60L, TimeUnit.SECONDS,
//        SynchronousQueue(),
//        ThreadFactory {
//            val threadNo = AtomicInteger()
//            Thread(it, "CoilCustomFetcherDispatcher-${threadNo.incrementAndGet()}").apply {
//                isDaemon = true
//            }
//        }
//    )
//    val clazz = Class.forName("java.util.concurrent.ScheduledThreadPoolExecutor\$DelayedWorkQueue")
//    val c = clazz.getDeclaredConstructor()
//    c.isAccessible = true
//    val workQueue = c.newInstance() as BlockingQueue<java.lang.Runnable>
//    c.isAccessible = false

//    val a = ThreadPoolExecutor(
//        8, 10,
//        60L, TimeUnit.SECONDS,
//        workQueue,
//        ThreadFactory {
//            val threadNo = AtomicInteger()
//            Thread(it, "CoilCustomFetcherDispatcher-${threadNo.incrementAndGet()}").apply {
//                isDaemon = true
//            }
//        }
//    )


//    val a = newFixedThreadPoolContext(8, "CoilCustomFetcherDispatcher").asExecutor()
//    val a = newCachedThreadPoolCoroutineDispatcher(8, "Test").executor
//    repeat(200) {
//        a.execute {
////            Thread.sleep((Random.nextLong(5) + 1) * 1000)
//            println("repeat:$it, threadName:${Thread.currentThread().name}")
//        }
//
//    }

//    val text = "///https://www.\\baidu.com///"
//    println(text.replace(Regex("^/+"), ""))
//    println(text.replace(Regex("/+$"), ""))
//    println(text.replace(Regex("//+"), "/"))

//    testRequest()
//    while (true) {
//
//    }

//    RequestBuilder.newBuilder()
//        .url("xxxxx")
//        .method("GET")
//        .build().enqueue {
//            it.transform<Any>()
//        }
//    testTransform()
//    repeat(100) {
//        println(it)
//        testRequest()

//    }
//    while (true) {
//
//    }
//    runBlocking {
//        delay(5000)
//    }
//    GET, HEAD不能有Body
    //POST,PUT,PATCH.DELETE 可以有body
    testSer()
}

@OptIn(InternalSerializationApi::class, ExperimentalStdlibApi::class)
fun testSer() {
    val data = """
        [
        {"id":1, "name":"abc"},
        {"id":2, "name":"fggg"},
        {"id":3, "name":"srtg"}
        ]
    """.trimIndent()
//    ResponseContentNegotiation gson {
//
//    }
    val response = Response(
        requestInfo = RequestBuilder.newBuilder().build(),
        code = 0,
        headers = emptyMap(),
        body = Response.Body(data = data.toByteArray(), dataStr = data, charset = "UTF-8"),
        requestTime = 0L,
        responseTime = 0L
    )
   val resp =  response body transformTo<List<TestModel>>()
    println(resp)
}

@Serializable
data class TestModel(
    val id:Int,
    val name:String
)

private fun testRequest() {
    Requester.config(ConfigBuilder().ignoreSSLCertification(true)
        .headers(HashMap<String, Any>().apply {
            put("globalHeader1", 1)
            put("globalHeader2", 2)
        }
        ).params(HashMap<String, Any>().apply {
            put("globalParam3", 3)
            put("globalParam4", 4)
        }).requestProcessor {
            it.newBuilder().appendHeader("t", System.currentTimeMillis()).build()
        }
//        .responseListener {
//            println(it)
//        }
    )
    repeat(200) {
        runBlocking {
            val response = HttpGet from "https://www.baidu.com/" prohibit {
                RequestEntity.PROHIBIT_FLAG_ALL_EXCLUDE_RESPONSE_INTERCEPTOR
            } where {

            } enqueue {
                println(it.body?.dataStr)

            }
//        val response = RequestBuilder.newBuilder()
//            .url("https://www.baidu.com")
//            .get()
//            .build().call()
        }
    }
    runBlocking {
        delay(5 * 1000)
    }
}

private fun testTransform() {


    val data = """

            [
            {"uid":"1222222","nickname":"abcdddd","avatar":"xxx.png"},
            {"uid":"1222222","nickname":"abcdddd","avatar":"xxx.png"},
            {"uid":"1222222","nickname":"abcdddd","avatar":"xxx.png"},
            {"uid":"1222222","nickname":"abcdddd","avatar":"xxx.png"},
            {"uid":"1222222","nickname":"abcdddd","avatar":"xxx.png"}
            ]
        """.trimIndent().toByteArray()
    val response = Response(
        requestInfo = RequestBuilder.newBuilder().build(), code = 0, headers = emptyMap(),
        body = Response.Body(data = data, dataStr = String(data, Charset.forName("UTF-8")), charset = "UTF-8"), 0L, 0L
    )

//    val user = response.transform<List<User>>(object : TypeToken<Any>::class.java) {
//
//    }
//    response.transform<List<User>>()
//    response transformTo String::class.java
//    val user = response transformTo typeOf<List<User>>()
//    val user = response.transform<List<User>>()
//    val user = response.transform<UserList>()
//    response.transform<Map<String, String>>()

//    val user = response transformTo UserList::class.java get {
//        this
//    }

//    HttpGet from "https://www.baidu.com" sync call transformTo User::class.java get { this?.uid }


//    println(user)
}

data class UserList(val count: Int, val hasMore: Boolean, val list: List<User>)

data class User(val uid: String, val nickname: String, val avatar: String)