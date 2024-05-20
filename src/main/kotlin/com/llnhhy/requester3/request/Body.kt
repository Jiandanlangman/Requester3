package com.llnhhy.requester3.request

import java.util.UUID

class Body(val contentType:String, val data:ByteArray) {

    private val uuid = UUID.randomUUID().toString().replace("-", "")

    override fun toString(): String {
        return "Body(contentType='$contentType', length=${data.size}, uuid='$uuid')"
    }
}