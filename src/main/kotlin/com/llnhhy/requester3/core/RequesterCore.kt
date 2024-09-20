package com.llnhhy.requester3.core

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import java.net.InetAddress
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

interface RequesterCore {


    suspend fun call(entity: RequestEntity) : Response


}