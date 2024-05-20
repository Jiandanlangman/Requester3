# Requester3


### 快速上手
任选一种请求方式即可。</br>
必须在suspend代码块中执行同步请求。
```kotlin
//kotlin同步请求
val user = RequestBuilder.newBuilder()
    .url("user/info")
    .get()
    .appendQuery("id", 12345)
    .build()
    .call()
    .transformTo(typeOf<User>())


//DSL同步请求
HttpGet from "user/info" where {
        query { "id" to 12345 }
    } sync call transformTo typeOf<UserInfo>()


//kotlin异步请求
RequestBuilder.newBuilder()
    .url("user/info")
    .get()
    .appendQuery("id", 12345)
    .build()
    .enqueue {
        val user = it.transformTo(typeOf<User>())
    }


//DSL异步请求
HttpGet from "user/info" where {
    query { "id" to 12345 }
} enqueue {
   val user = it transformTo typeOf<UserInfo>()
}
```
