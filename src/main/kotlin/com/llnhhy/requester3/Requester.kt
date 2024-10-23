package com.llnhhy.requester3

import com.llnhhy.requester3.config.Config
import com.llnhhy.requester3.config.ConfigBuilder
import com.llnhhy.requester3.core.OKHttpCoreFactory
import com.llnhhy.requester3.core.RequesterCoreFactory
import com.llnhhy.requester3.interceptor.Interceptor
import com.llnhhy.requester3.request.*
import com.llnhhy.requester3.response.Response
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


object Requester {


    private val requesterScope = CoroutineScope(Dispatchers.Unconfined)

    private val customCoreMutex = Mutex()

    private var coreFactory: RequesterCoreFactory? = null

    internal var config: Config = Config()
        private set

    private val interceptor = object : Interceptor {

        override suspend fun proceed(entity: RequestEntity): Response {
            return coreCall(entity)
        }

        override fun cancel(entity: RequestEntity): Response {
            val t = System.currentTimeMillis()
            return Response(
                requestInfo = entity,
                code = Response.CODE_REQUEST_CANCELED,
                headers = emptyMap(),
                body = null,
                requestTime = t,
                responseTime = t
            )
        }


    }

    infix fun coreFactory(factory: RequesterCoreFactory) = apply {
        requesterScope.launch(Dispatchers.Default) {
            customCoreMutex.withLock {
                coreFactory = factory
            }
        }
    }


    infix fun config(builder: ConfigBuilder.() -> Unit) = apply {
        config = ConfigBuilder().apply(builder).build()
    }


    suspend fun call(entity: RequestEntity): Response {
        return config.interceptor.invoke(interceptor, entity)
    }

    fun enqueue(
        entity: RequestEntity,
        response: NotCallerThread.(Response) -> Unit
    ) = requesterScope.launch(Dispatchers.IO) {
        response(NotCallerThread, call(entity))
    }


    private suspend fun coreCall(entity: RequestEntity): Response {
        val requesterCore = customCoreMutex.withLock {
            (coreFactory ?: OKHttpCoreFactory).getRequesterCore(X509TrustManagerDelegate.getSslContext().socketFactory, X509TrustManagerDelegate, HostnameVerifierDelegate, DnsDelegate)
        }
        return requesterCore.call(entity)
    }

}