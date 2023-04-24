package github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import github.apiFormats.AccessTokenResponse
import github.apiFormats.Branch
import github.apiFormats.RepositoryContents
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.call.body
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import kotlinx.coroutines.runBlocking
import utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.util.*

class GithubAPI {

    private val client = HttpClient(Apache)
    private val baseUrl = "https://api.github.com"

    private fun buildRequest(
        url: String, method: HttpMethod, accessToken: String, body: String? = null
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            this.url("$baseUrl$url")
            this.method = method
            this.header(HttpHeaders.Authorization, "Bearer $accessToken")
            this.accept(ContentType.Application.Json)
            this.header("X-GitHub-Api-Version", "2022-11-28")
            if (body != null) {
                this.setBody(TextContent(body, ContentType.Application.Json))
                println("text body ${TextContent(body, ContentType.Application.Json)}")
            }
        }
    }

    fun HttpRequestBuilder.prettyPrint(): String {
        val method = this.method
        val urlBuilder = this.url
        val url = URLBuilder(urlBuilder).buildString()
        val headers = this.headers.entries().joinToString("\n") { (key, values) ->
            val headerValues = values.joinToString(", ")
            "\t$key: $headerValues"
        }

        return buildString {
            append("Request Details:\n")
            append("Method: $method\n")
            append("URL: $url\n")
            append("Headers:\n$headers")
        }
    }

    fun createPullRequest(
        accessToken: String, owner: String, repo: String, title: String, body: String, head: String, base: String
    ) {
        val apiUrl = "/repos/$owner/$repo/pulls"
        val messageBody = "{\"title\":\"$title\",\"body\":\"$body\",\"head\":\"$head\",\"base\":\"$base\"}"
        val request = buildRequest(apiUrl, HttpMethod.Post, accessToken, messageBody)
        println("Request is ${request.prettyPrint()}")
        val response = runBlocking { client.request(request) }

        if (!response.status.isSuccess()) {
            println("Error creating pull request: ${response.status}")
        } else {
            println("Success $response")
        }
    }

    fun getFileContent(accessToken: String, owner: String, repo: String, path: String): RepositoryContents {
        val url = "/repos/$owner/$repo/contents/$path"
        println("Getting file content from $url")
        val request = buildRequest(url, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request).bodyAsText() }
        return Gson().fromJson(response, RepositoryContents::class.java)
    }

    fun updateContent(
        owner: String,
        repo: String,
        path: String,
        message: String,
        content: String,
        sha: String,
        accessToken: String,
        branch: String
    ): String {
        val contentUrl = "/repos/$owner/$repo/contents/$path"
        val requestBody = "{\"message\":\"$message\",\"content\":\"$content\",\"sha\":\"$sha\", \"branch\":\"$branch\"}"
        val request = buildRequest(contentUrl, HttpMethod.Put, accessToken, requestBody)

        return runBlocking { client.request(request).bodyAsText() }
    }

    fun createBranch(
        owner: String, repo: String, newBranchName: String, shaToBranchFrom: String, accessToken: String
    ) {
        val gitRefUrl = "/repos/$owner/$repo/git/refs"
        val requestBody = "{\"ref\":\"refs/heads/${newBranchName}\",\"sha\":\"$shaToBranchFrom\"}"
        val request = buildRequest(gitRefUrl, HttpMethod.Post, accessToken, requestBody)
        val response = runBlocking { client.request(request).bodyAsText() }
        println("created branch $response")
    }

    fun getBranches(
        owner: String, repo: String, accessToken: String
    ): List<Branch> {
        val branchesUrl = "/repos/$owner/$repo/branches"
        val request = buildRequest(branchesUrl, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request).bodyAsText() }

        return Gson().fromJson(response, Array<Branch>::class.java).toList()
    }

    fun cloneRepo(accessToken: String, owner: String, repo: String, outputDir: String): String? {
        val tokenUrl = "/repos/$owner/$repo/zipball"
        val request = buildRequest(tokenUrl, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request) }

        if (response.status.isSuccess()) {
            val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
            val fileName = contentDisposition?.substringAfter("filename=")?.removeSurrounding("\"")
            val zipFile = if (fileName != null) {
                File(outputDir, fileName)
            } else {
                File.createTempFile("temp-", ".zip", File(outputDir))
            }
            val content = runBlocking { response.body<ByteArray>() }
            FileOutputStream(zipFile).use { fileOut ->
                fileOut.write(content)
            }
            Utils.extractZipFile(zipFile, outputDir)
            return fileName
        } else {
            println("Error downloading ZIP file: ${response.status}")
            return null
        }
    }

    private fun getAccessToken(tokenUrl: String, jwtToken: String): AccessTokenResponse {
        val client = HttpClient(Apache)
        val httpRequestBuilder = HttpRequestBuilder()
        httpRequestBuilder.url(tokenUrl)
        httpRequestBuilder.method = HttpMethod.Post
        httpRequestBuilder.contentType(ContentType.Application.Json)
        httpRequestBuilder.header(HttpHeaders.Authorization, "Bearer $jwtToken")
        httpRequestBuilder.accept(ContentType.Application.Json)
        val body = runBlocking { client.request(httpRequestBuilder).bodyAsText() }

        return Gson().fromJson(body, AccessTokenResponse::class.java)
    }

    fun fetchAccessToken(baseUrl: String, appId: String, algorithm: Algorithm, installationId: Int): String {
        val tokenUrl = "$baseUrl/app/installations/$installationId/access_tokens"
        val jwtToken = createJwtToken(appId, algorithm)
        val accessTokenResponse = getAccessToken(tokenUrl, jwtToken)

        return accessTokenResponse.token
    }

    private fun createJwtToken(appId: String, algorithm: Algorithm): String {
        return JWT.create().withIssuer(appId).withIssuedAt(Date(System.currentTimeMillis() - 500000))
            .withExpiresAt(Date(System.currentTimeMillis() + 500000)).sign(algorithm)
    }
}