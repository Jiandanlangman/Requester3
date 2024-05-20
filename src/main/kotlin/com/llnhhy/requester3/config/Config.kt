package com.llnhhy.requester3.config

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.NotCallerThread
import kotlinx.coroutines.CoroutineDispatcher
import java.net.InetAddress






open class Config internal constructor(
    val baseUrl: String = "",
    val method: String = "GET",
    val timeout: Timeout = Timeout.Default,
    val ignoreSSLCertification: Boolean = false,
    val dns: ((String) -> List<InetAddress>)? = null,
    val headers: Map<String, Any> = emptyMap(),
    val params: Map<String, Any> = emptyMap(),
    val requestProcessor: suspend NotCallerThread.(RequestEntity) -> RequestEntity? = { it },
    val responseInterceptor: suspend NotCallerThread.(Response) -> Boolean = { false },
    val dispatcher: CoroutineDispatcher?= null
)