package com.llnhhy.requester3.core

import com.llnhhy.requester3.DnsDelegate
import com.llnhhy.requester3.HostnameVerifierDelegate
import com.llnhhy.requester3.Requester.config
import com.llnhhy.requester3.Requester.requestTimeoutProperties
import com.llnhhy.requester3.X509TrustManagerDelegate
import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.collections.get

object RequesterCoreOKHttp : RequesterCore {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor {
//            val timeOut = requestTimeoutProperties[it.request().tag()] ?: config.timeout
//            it.withReadTimeout(timeOut.read, TimeUnit.MILLISECONDS)
//                .withConnectTimeout(timeOut.connect, TimeUnit.MILLISECONDS)
//                .withWriteTimeout(timeOut.write, TimeUnit.MILLISECONDS)
//                .proceed(it.request())
            it.proceed(it.request())
        }
//        .dispatcher(Dispatcher(Executors.newSingleThreadExecutor()))
//        .sslSocketFactory(X509TrustManagerDelegate.getSslContext().socketFactory, X509TrustManagerDelegate)
//        .hostnameVerifier(HostnameVerifierDelegate)
//        .dns(DnsDelegate)
        .build()

    override suspend fun call(entity: RequestEntity): Response {
        TODO("Not yet implemented")
    }

    override fun setSSLSocketFactory(sslSocketFactory: SSLSocketFactory, trustManager: X509TrustManager) {
        TODO("Not yet implemented")
    }

    override fun setHostnameVerifier(hostnameVerifier: HostnameVerifier) {
        TODO("Not yet implemented")
    }

    override fun setDNS(dns: (String) -> List<InetAddress>) {
        TODO("Not yet implemented")
    }
}