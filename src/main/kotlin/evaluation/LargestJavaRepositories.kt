package evaluation

import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

fun main() {
    val client = OkHttpClient()
    val json = Json { ignoreUnknownKeys = true }
    val randomIndices = (0..19).shuffled()

    // start from 1MB (1024KB), increment by 1MB for each request
    for (size in 1024..20480 step 1024) {
        val request = Request.Builder()
            .url("https://api.github.com/search/repositories?q=language:java+size:$size..${size+1023}&sort=stars&order=desc")
            .build()

        val response = client.newCall(request).execute()
        response.use {
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val body = response.body.string()
            val jsonObject = json.parseToJsonElement(body).jsonObject
            val items = jsonObject["items"]!!.jsonArray
            val repositories = items.map { json.decodeFromJsonElement<Repository>(it) }

            // Print the name of the first repository for this size range
            if (repositories.isNotEmpty()) {
                val randomRepository = repositories[randomIndices[size / 1024]]
                println("Name: ${randomRepository.name}, Full Name: ${randomRepository.full_name}, Size: ${randomRepository.size}, URL: ${randomRepository.html_url}")
            }
        }
    }
}
