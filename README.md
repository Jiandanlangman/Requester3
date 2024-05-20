# Requester3


### 快速上手
任选一种请求方式即可。
#### 同步请求
同步请求必须在suspend代码块中执行。
```kotlin
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

```
#### 异步请求
异步请求返回线程是Requester内部线程而非调用者线程；如果需要切换回调用者线程，可以手动切换。
```kotlin
//kotlin
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
