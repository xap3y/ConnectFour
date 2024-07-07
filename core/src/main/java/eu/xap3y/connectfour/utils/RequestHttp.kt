package eu.xap3y.connectfour.utils

import eu.xap3y.connectfour.ConnectFour
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.CompletableFuture

object RequestHttp{

    fun isNewest(): CompletableFuture<Pair<Boolean, String?>> {

        val client = OkHttpClient()

        return CompletableFuture.supplyAsync {
            val request = Request.Builder()
                .url(ConnectFour.VERSION_UPSTREAM_URL)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@supplyAsync Pair(false, null)
                }


                val body = response.body.string()
                val verNewest = body.replace(".", "")

                if (ConnectFour.VERSION.replace(".", "").toInt() < verNewest.toInt()) {
                    Pair(false, body)
                } else Pair(true, null)
            }
        }
    }
}