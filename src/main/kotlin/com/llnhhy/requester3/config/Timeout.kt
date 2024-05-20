package com.llnhhy.requester3.config

data class Timeout(
    val connect: Int = 5 * 1000,
    val write: Int = 5 * 1000,
    val read: Int = 10 * 1000
) {
    companion object {
        val Default = Timeout()
    }
}