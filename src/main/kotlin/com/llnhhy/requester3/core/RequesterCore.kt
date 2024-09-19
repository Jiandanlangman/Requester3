package com.llnhhy.requester3.core

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import java.net.InetAddress
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface RequesterCore {

//  companion object {
//        abstract fun A()
//    }

    suspend fun call(entity: RequestEntity) : Response

//    fun setSSLSocketFactory(sslSocketFactory: SSLSocketFactory, trustManager: X509TrustManager)
//
//    fun setHostnameVerifier(hostnameVerifier: HostnameVerifier)
//
//    fun setDNS(dns:(String) -> List<InetAddress>)

}