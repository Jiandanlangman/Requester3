package com.llnhhy.requester3.config

import com.llnhhy.requester3.interceptor.Interceptor
import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import java.net.InetAddress



open class Config internal constructor(
    val baseUrl: String = "",
    val defaultMethod: String = "GET",
    val timeout: Timeout = Timeout.Default,
    val ignoreSSLCertification: Boolean = false,
    val dns: ((String) -> List<InetAddress>)? = null,
    val interceptor: suspend Interceptor.(RequestEntity) -> Response = { proceed(it) }
)