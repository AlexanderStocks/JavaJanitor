package github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import github.apiFormats.AccessTokenResponse
import github.apiFormats.Branch
import github.apiFormats.RepositoryContents
import github.apiFormats.commit.Blob
import github.apiFormats.commit.Commit
import github.apiFormats.commit.Tree
import github.apiFormats.commit.TreeEntry
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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.kohsuke.github.GHCommit
import org.kohsuke.github.GitHub
import org.kohsuke.github.GHAppInstallation

import utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.*

class GithubAPI {

    private var accessToken = ""
    private val client = HttpClient(Apache)
    private val baseUrl = "https://api.github.com"

    private fun buildRequest(
        url: String, method: HttpMethod, body: String? = null
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            this.url("$baseUrl$url")
            this.method = method
            this.header(HttpHeaders.Authorization, "Bearer $accessToken")
            this.accept(ContentType.Application.Json)
            this.header("X-GitHub-Api-Version", "2022-11-28")
            if (body != null) {
                this.setBody(TextContent(body, ContentType.Application.Json))
                println("text body ${this.body}")
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
            append("Method: ${method.value}\n")
            append("URL: $url\n")
            append("Headers:\n$headers")
        }
    }

    fun createPullRequest(
        owner: String, repo: String, title: String, body: String, head: String, base: String
    ) {
        println("Creating pull request with base branch: $base, head branch: $head")

        val apiUrl = "/repos/$owner/$repo/pulls"
        val messageBody = "{\"title\":\"$title\",\"body\":\"$body\",\"head\":\"$head\",\"base\":\"$base\"}"
        val request = buildRequest(apiUrl, HttpMethod.Post, messageBody)
        println("Request is ${request.prettyPrint()}")
        val response = runBlocking { client.request(request) }

        if (!response.status.isSuccess()) {
            println("Error creating pull request: ${response.status}, ${runBlocking { response.bodyAsText()}}")
        } else {
            println("Success $response")
        }
    }

    fun getFileContent(owner: String, repo: String, path: String): RepositoryContents {
        val url = "/repos/$owner/$repo/contents/$path"
        println("Getting file content from $url")
        val request = buildRequest(url, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request).bodyAsText() }
        return Gson().fromJson(response, RepositoryContents::class.java)
    }

    suspend fun updateContent(
        owner: String,
        repo: String,
        path: String,
        message: String,
        content: String,
        sha: String,
        branch: String
    ): String {
        val contentUrl = "/repos/$owner/$repo/contents/$path"
        val requestBody = "{\"message\":\"$message\",\"content\":\"$content\",\"sha\":\"$sha\", \"branch\":\"$branch\"}"
        val request = buildRequest(contentUrl, HttpMethod.Put, requestBody)

        return client.request(request).bodyAsText()
    }



    fun createBranch(
        owner: String, repo: String, newBranchName: String, shaToBranchFrom: String
    ) {
        val gitRefUrl = "/repos/$owner/$repo/git/refs"
        val requestBody = "{\"ref\":\"refs/heads/${newBranchName}\",\"sha\":\"$shaToBranchFrom\"}"
        val request = buildRequest(gitRefUrl, HttpMethod.Post, requestBody)
        val response = runBlocking { client.request(request).bodyAsText() }
        println("created branch $response")
    }

    fun getBranches(
        owner: String, repo: String
    ): List<Branch> {
        val branchesUrl = "/repos/$owner/$repo/branches"
        val request = buildRequest(branchesUrl, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request).bodyAsText() }

        return Gson().fromJson(response, Array<Branch>::class.java).toList()
    }

    fun cloneRepo(owner: String, repo: String, outputDir: String): String? {
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
        println(body)
        return Gson().fromJson(body, AccessTokenResponse::class.java)
    }

    fun fetchAccessToken(baseUrl: String, appId: String, algorithm: Algorithm, installationId: Int): String {
        val tokenUrl = "$baseUrl/app/installations/$installationId/access_tokens"
        val jwtToken = createJwtToken(appId, algorithm)
        println("JWT token is $jwtToken, token url is $tokenUrl")
        val accessTokenResponse = getAccessToken(tokenUrl, jwtToken)

        return accessTokenResponse.token
    }

    private fun createJwtToken(appId: String, algorithm: Algorithm): String {
        return JWT.create().withIssuer(appId).withIssuedAt(Date(System.currentTimeMillis() - 500000))
            .withExpiresAt(Date(System.currentTimeMillis() + 500000)).sign(algorithm)
    }

    suspend fun createBlob(
        owner: String,
        repo: String,
        content: String,
        accessToken: String
    ): Blob {
        val blobUrl = "/repos/$owner/$repo/git/blobs"
        val requestBody = "{\"content\":\"$content\",\"encoding\":\"base64\"}"
        val request = buildRequest(blobUrl, HttpMethod.Post, requestBody)

        val response = client.request(request)
        println("Blob response is $response")
        return Gson().fromJson(response.bodyAsText(), Blob::class.java)
    }

    suspend fun createTree(
        owner: String,
        repo: String,
        treeEntries: List<TreeEntry>
    ): Tree {
        val treeUrl = "/repos/$owner/$repo/git/trees"
        val requestBody = Gson().toJson(
            mapOf(
                "tree" to treeEntries.map {
                    mapOf(
                        "path" to it.path,
                        "mode" to it.mode,
                        "type" to "blob",
                        "sha" to it.sha
                    )
                }
            )
        )
        val request = buildRequest(treeUrl, HttpMethod.Post, requestBody)
        val response = client.request(request)
        println("Tree response is $response")
        return Gson().fromJson(response.bodyAsText(), Tree::class.java)
    }

    suspend fun createCommit(
        owner: String,
        repo: String,
        message: String,
        treeSha: String,
        parentCommitSha: String,
    ): Commit {
        val commitUrl = "/repos/$owner/$repo/git/commits"
        val requestBody = Gson().toJson(
            mapOf(
                "message" to message,
                "tree" to treeSha,
                "parents" to listOf(parentCommitSha)
            )
        )
        val request = buildRequest(commitUrl, HttpMethod.Post, requestBody)
        println("Commit request is ${requestBody}")
        val response = client.request(request)
        println("Commit response body is ${response.bodyAsText()} ${response.status}.")
        return Gson().fromJson(response.bodyAsText(), Commit::class.java)
    }

    suspend fun updateRef(
        owner: String,
        repo: String,
        ref: String,
        commitSha: String
    ) {
        val refUrl = "/repos/$owner/$repo/git/refs/$ref"
        val requestBody = "{\"sha\":\"$commitSha\"}"
        val request = buildRequest(refUrl, HttpMethod.Patch, requestBody)
        val response = client.request(request)
        println("Update ref response is $response")
    }

    private suspend fun getLatestCommitSha(
        owner: String,
        repo: String,
        branch: String,
    ): String {
        val branchUrl = "/repos/$owner/$repo/branches/$branch"
        val request = buildRequest(branchUrl, HttpMethod.Get, accessToken)

        val response = client.request(request)
        println("Branch info response is $response")

        // We assume that the response is successfully received and it's in the expected format.
        // Error handling should be added here according to your requirements.
        val branchInfo = Gson().fromJson(response.bodyAsText(), Branch::class.java)

        return branchInfo.commit.sha
    }

    public fun setAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }
}