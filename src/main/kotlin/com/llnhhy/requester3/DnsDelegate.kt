package com.llnhhy.requester3

import java.net.InetAddress

internal object DnsDelegate : DNS  {


     override fun lookup(hostname: String): List<InetAddress> {
        Requester.config.dns?.let {
            return it(hostname)
        }
        return InetAddress.getAllByName(hostname).toList()
    }
}