package com.llnhhy.requester3

import okhttp3.internal.platform.Platform
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

internal object X509TrustManagerDelegate : X509TrustManager {

    private val platformX509TrustManager = Platform.get().platformTrustManager()


    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if(!Requester.config.ignoreSSLCertification) {
            platformX509TrustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        if(!Requester.config.ignoreSSLCertification) {
            platformX509TrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return if(!Requester.config.ignoreSSLCertification) {
            platformX509TrustManager.acceptedIssuers
        } else {
            emptyArray()
        }
    }

    fun getSslContext() : SSLContext {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(this), SecureRandom())
        return sslContext
    }

}