package com.llnhhy.requester3.core

import com.llnhhy.requester3.DNS
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object OKHttpCoreFactory : RequesterCoreFactory {

    private var sslSocketFactory: SSLSocketFactory? = null
    private var trustManager : X509TrustManager? = null
    private var hostnameVerifier : HostnameVerifier? = null
    private var dns:DNS? = null
    private var okHttpCore: OKHttpCore? = null

    override fun getRequesterCore(sslSocketFactory: SSLSocketFactory, trustManager: X509TrustManager, hostnameVerifier: HostnameVerifier, dns: DNS): RequesterCore {
        if(!createNew(sslSocketFactory, trustManager, hostnameVerifier, dns)) {
            return okHttpCore!!
        }
        okHttpCore?.close()
        return OKHttpCore(sslSocketFactory, trustManager, hostnameVerifier, dns).apply {
            okHttpCore = this
        }
    }

    private fun createNew(sslSocketFactory: SSLSocketFactory, trustManager: X509TrustManager, hostnameVerifier: HostnameVerifier, dns: DNS) : Boolean {
        if (okHttpCore == null) {
            return true
        }
        return this.sslSocketFactory != sslSocketFactory || this.trustManager != trustManager || this.hostnameVerifier != hostnameVerifier || this.dns != dns
    }
 }