package com.llnhhy.requester3.core

import com.llnhhy.requester3.DNS
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface RequesterCoreFactory {

    fun getRequesterCore(sslSocketFactory: SSLSocketFactory,
                         trustManager: X509TrustManager,
                         hostnameVerifier: HostnameVerifier,
                         dns: DNS) : RequesterCore

}