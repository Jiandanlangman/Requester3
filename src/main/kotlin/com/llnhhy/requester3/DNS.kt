package com.llnhhy.requester3

import java.net.InetAddress

interface DNS {

    fun lookup(hostname: String): List<InetAddress>

}