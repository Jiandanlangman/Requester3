package com.llnhhy.requester3

import com.llnhhy.requester3.download.Downloader
import com.llnhhy.requester3.download.globalScope
import com.llnhhy.requester3.negotiation.*
import com.llnhhy.requester3.request.*
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable


object A {
    var a = "This is A"
    init {
        B.b = "dasdasdasda"
        println(B.b)
    }
}

object B {
    var b = "THIS IS B"
    init {
        A.a = "dadasdasdas"
       println( A.a)
    }
}




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
    A.toString()

//    runBlocking {
//        globalScope.launch(Dispatchers.IO) {
//                        SocketServer.activeClients.collect {
//                println("客户端连接状态发生改变，当前已连接客户端:${it.keys.map { it.asLongText() }}")
//            }
//        }
//        SocketServer.registerMessageReceiver { clientId, msg ->
//            println("收到来自客户端${clientId.asLongText()}的消息:$msg")
//            globalScope.launch {
//                SocketServer.sendMessage(clientId, "给你回一条")
//            }
//        }
//        val port = SocketServer.start()
//        TestSocketClient.collect("localhost", port)
//    }
//    runBlocking {
//       val downloadFile = Downloader(url = "https://dldir1.qq.com/qqfile/qq/QQNT/Windows/QQ_9.9.15_240925_arm64_01.exe",
//            progress = { contentLength, downloadedLength ->
//                println("文件大小:${contentLength}, 下载进度:${String.format("%.2f", downloadedLength.toDouble() / contentLength)}")
//            }, errorHandler = {
//                it.printStackTrace()
//            }).download()
//        //downloadFile不为空就表示下载成功了
//        println("下载完成:${downloadFile?.absolutePath}")
//    }
//    testSer()
//    testRequest()
}

private fun testRequest() {
    Requester config {
        baseUrl("https://api.xxx.xxx.com") //url前缀，设置后发起请求时url可不带前缀
        interceptor {//拦截器
            println("interceptor:${it.url}")
            val newBuilder = it.newBuilder().headers {
                //全局Header
                put("adad", "23123")
            }.formData {
                //全局formData，method为GET和HEAD时不生效
            }.queries {
                //全局query
            }
            val response = proceed(newBuilder.build()) //请求接口并拿到返回值, 如果需要取消请求，可以不调用这一行，手动构建一个Response返回
            response.throwable?.printStackTrace() //如果有异常就打印出异常
            val str = response.body<String>()
            println(str) //打印日志
            response//返回给请求框架
        }
        //还有其它方法，可以调用this. 看看ide有什么提示
    }
    runBlocking {
        val response = HttpPost from "https://www.baidu.com/" queries {
            put { "1" to 2 }
        } headers {
            put { "2" to "dadsa" }
        } formData {
            put { "dasds" to "dasdasdas" }
        } call sync bodyTransformTo typeOf<String>()
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
    val id: Int,
    val name: String,
    val labels: List<Label>
)

@Serializable
data class Label(val icon: String, val name: String, val sort: Int)

