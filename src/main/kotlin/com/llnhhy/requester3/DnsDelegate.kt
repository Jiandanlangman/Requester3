package com.llnhhy.requester3

import okhttp3.Dns
import java.net.InetAddress

internal object DnsDelegate : Dns {

    private val platformDns = Dns.SYSTEM

    override fun lookup(hostname: String): List<InetAddress> {
        Requester.config.dns?.let {
            return it(hostname)
        }
        return platformDns.lookup(hostname)
    }
}