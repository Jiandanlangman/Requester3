package com.llnhhy.requester3.core

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response

interface RequesterCore {


    suspend fun call(entity: RequestEntity) : Response


}