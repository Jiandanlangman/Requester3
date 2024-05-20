package com.llnhhy.requester3

import okhttp3.internal.tls.OkHostnameVerifier
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

internal object HostnameVerifierDelegate : HostnameVerifier {

    private val platformHostnameVerifier = OkHostnameVerifier

    override fun verify(hostname: String, session: SSLSession): Boolean {
        return if(!Requester.config.ignoreSSLCertification) {
            platformHostnameVerifier.verify(hostname, session)
        } else {
            true
        }
    }


}