package com.llnhhy.requester3.download


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

class Downloader(
    private val url: String, private val progress: (contentLength: Long, downloadedLength: Long) -> Unit = { _, _ -> }, private val errorHandler: (Throwable) -> Unit = {
        it.printStackTrace()
    }
) {

    private companion object {
        val client = OkHttpClient.Builder().build()

        fun md5(data: ByteArray): String {
            val sb = StringBuilder("")
            val digest = MessageDigest.getInstance("MD5")
            val md5ByteArray = digest.digest(data)
            md5ByteArray.forEach {
                val hexString = Integer.toHexString(it.toInt() and 0xFF)
                if (hexString.length < 2)
                    sb.append("0")
                sb.append(hexString)
            }
            return sb.toString().uppercase()
        }

    }

    suspend fun download(): File? {
        return withContext(Dispatchers.IO) {
            val file = File(md5(url.toByteArray()))
            val accessFile = RandomAccessFile(file, "rw")
            val fileLength = file.length()
            val request = Request.Builder().url(url)
                .apply {
                    if (file.length() != 0L) {
                        header("Range", "bytes=${fileLength}-")
                    }
                }
                .build()
            var response: Response? = null
            try {
                response = client.newCall(request).execute().apply {
                    val responseCode = code
                    if (responseCode != 200 && responseCode != 206) {
                        errorHandler(RuntimeException("response code:$responseCode"))
                        return@withContext null
                    }
                    var contentLength: Long
                    var downloadedLength: Long
                    if (responseCode == 206) {
                        accessFile.seek(fileLength)
                        contentLength = fileLength + body!!.contentLength()
                        downloadedLength = fileLength
                    } else {
                        accessFile.seek(0)
                        contentLength = body!!.contentLength()
                        downloadedLength = 0
                    }
                    val inputStream = body!!.byteStream()
                    val buffer = ByteArray(81960)
                    var readLength: Int
                    while (inputStream.read(buffer).also { readLength = it } != -1) {
                        accessFile.write(buffer, 0, readLength)
                        downloadedLength += readLength
                        progress(contentLength, downloadedLength)
                    }
                    return@withContext file
                }
            } catch (tr: Throwable) {
                errorHandler(tr)
            } finally {
                try {
                    response?.close()
                } catch (ignore: Throwable) {

                }
            }
            null
        }

    }


}

val globalScope = CoroutineScope(Dispatchers.IO)



class Test {

    private val cmdChannel = Channel<String>(capacity = Int.MAX_VALUE)


    init {

        //
        globalScope.launch(Dispatchers.IO) {
            cmdChannel.consumeAsFlow().collect {
                //消费，
            }
        }
    }

    suspend fun sendCMD(cmd: String) {
        //发送
        cmdChannel.send(cmd)
    }

}