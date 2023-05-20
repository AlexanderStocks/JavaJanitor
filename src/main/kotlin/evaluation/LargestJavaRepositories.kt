package evaluation

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import java.io.IOException

fun main () {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.github.com/search/repositories?q=language:java&sort=size&order=desc")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val body = response.body.string()
                val json = Json { ignoreUnknownKeys = true }
                val jsonObject = json.parseToJsonElement(body).jsonObject
                val items = jsonObject["items"]!!.jsonArray
                val repositories = items.map { json.decodeFromJsonElement<Repository>(it) }

                repositories.forEach {
                    println("Name: ${it.name}, Full Name: ${it.full_name}, Size: ${it.size}, URL: ${it.html_url}")
                }
            }
        }
    })
}

