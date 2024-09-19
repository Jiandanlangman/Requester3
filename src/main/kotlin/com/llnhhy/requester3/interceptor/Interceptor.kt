package com.llnhhy.requester3.interceptor

import com.llnhhy.requester3.request.RequestEntity
import com.llnhhy.requester3.response.Response

interface Interceptor {

    suspend fun proceed(entity: RequestEntity) : Response

    fun cancel(entity: RequestEntity) : Response

}