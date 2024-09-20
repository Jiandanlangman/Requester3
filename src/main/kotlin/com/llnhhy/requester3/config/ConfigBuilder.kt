package com.llnhhy.requester3.config

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import com.llnhhy.requester3.interceptor.Interceptor
import java.net.InetAddress


class ConfigBuilder internal constructor() {

    private companion object {

        private val baseUrlRegex = Regex("/+$")


    }

    private class MutableConfig(
        var basePrefix: String,
        var defaultMethod: String,
        var timeout: Timeout,
        var ignoreSSLCertification: Boolean,
        var dns: ((String) -> List<InetAddress>)?,
        var interceptor: suspend Interceptor.(RequestEntity) -> Response
    ) {

        companion object {

            fun toMutableConfig(config: Config): MutableConfig {
                return MutableConfig(
                    config.baseUrl,
                    config.defaultMethod,
                    config.timeout,
                    config.ignoreSSLCertification,
                    config.dns,
                    config.interceptor
                )
            }

        }

        fun toConfig() = Config(
            basePrefix,
            defaultMethod,
            timeout,
            ignoreSSLCertification,
            dns,
            interceptor
        )
    }


    private val mutableConfig: MutableConfig = MutableConfig.toMutableConfig(Config())

    internal fun build() = mutableConfig.toConfig()

    fun baseUrl(baseUrl: String) = apply {
        mutableConfig.basePrefix = baseUrl.replace(baseUrlRegex, "")
    }

    fun defaultMethod(method: String) = apply {
        mutableConfig.defaultMethod = method
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


    fun interceptor(processor: suspend Interceptor.(RequestEntity) -> Response) = apply {
        mutableConfig.interceptor = processor
    }


}