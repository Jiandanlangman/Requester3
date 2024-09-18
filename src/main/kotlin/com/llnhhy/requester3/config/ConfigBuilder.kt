package com.llnhhy.requester3.config

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.NotCallerThread
import kotlinx.coroutines.CoroutineDispatcher
import java.net.InetAddress


class ConfigBuilder {

    private companion object {
        private val baseUrlRegex = Regex("/+$")

    }

    private class MutableConfig(
        var basePrefix: String,
        var method: String,
        var timeout: Timeout,
        var ignoreSSLCertification: Boolean,
        var dns: ((String) -> List<InetAddress>)?,
//        var headers: Map<String, Any>,
//        var params: Map<String, Any>,
        var requestProcessor: suspend NotCallerThread.(RequestEntity) -> RequestEntity?,
        var responseInterceptor: suspend NotCallerThread.(Response) -> Boolean,
        var dispatcher: CoroutineDispatcher?
    ) {

        companion object {


            fun toMutableConfig(config: Config): MutableConfig {
                return MutableConfig(
                    config.baseUrl,
                    config.method,
                    config.timeout,
                    config.ignoreSSLCertification,
                    config.dns,
//                    config.headers,
//                    config.params,
                    config.requestProcessor,
                    config.responseInterceptor,
                    config.dispatcher
                )
            }


        }

        fun toConfig() = Config(
            basePrefix,
            method,
            timeout,
            ignoreSSLCertification,
            dns,
//            headers,
//            params,
            requestProcessor,
            responseInterceptor,
            dispatcher
        )
    }


    private val mutableConfig: MutableConfig = MutableConfig.toMutableConfig(Config())

    fun build() = mutableConfig.toConfig()

    fun baseUrl(baseUrl: String) = apply {
        mutableConfig.basePrefix = baseUrl.replace(baseUrlRegex, "")
    }

    fun method(method: String) = apply {
        mutableConfig.method = method
    }

    fun timeout(timeout: Timeout) = apply {
        mutableConfig.timeout = timeout
    }

    fun ignoreSSLCertification(ignore: Boolean) = apply {
        mutableConfig.ignoreSSLCertification = ignore
    }

    fun dns(dns: (String) -> List<InetAddress>) = apply {
        mutableConfig.dns = dns
    }

//    fun headers(headers: Map<String, Any>) = apply {
//        mutableConfig.headers = headers
//    }
//
//    fun params(params: Map<String, Any>) = apply {
//        mutableConfig.params = params
//    }

    fun requestProcessor(processor: suspend NotCallerThread.(RequestEntity) -> RequestEntity) = apply {
        mutableConfig.requestProcessor = processor
    }

    fun responseInterceptor(interceptor: suspend NotCallerThread.(Response) -> Boolean) = apply {
        mutableConfig.responseInterceptor = interceptor
    }

    fun dispatcher(dispatcher: CoroutineDispatcher) = apply {
        mutableConfig.dispatcher = dispatcher
    }
}