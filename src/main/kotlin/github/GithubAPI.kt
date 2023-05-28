package github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import github.apiFormats.AccessTokenResponse
import github.apiFormats.Branch
import github.apiFormats.Repository
import github.apiFormats.commit.Commit
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
import org.kohsuke.github.GHRef
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHTree
import org.kohsuke.github.GHTreeBuilder
import org.kohsuke.github.GHTreeEntry

import utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.util.*

class GithubAPI(
    private val baseUrl: String,
    private val appId: String,
    private val algorithm: Algorithm,
    private val installationId: Int,
    private val repository: Repository
) {

    private var accessToken = ""
    private val client = HttpClient(Apache)
    private lateinit var ghRepository : GHRepository

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

    private var tokenExpirationTime = 0L

    fun getRef(owner: String): GHRef {
        refreshTokenIfNeeded()
        return ghRepository.getRef(owner)
    }

    fun getTree(owner: String, repo: String, sha: String): GHTree {
        refreshTokenIfNeeded()
        val treeUrl = "/repos/$owner/$repo/git/trees/$sha"
        val request = buildRequest(treeUrl, HttpMethod.Get)
        val response = runBlocking { client.request(request).bodyAsText() }
        return Gson().fromJson(response, GHTree::class.java)
    }

    fun createTree(owner: String, repo: String, baseTreeSha: String, treeEntries: List<GHTreeEntry>): GHTree {
        refreshTokenIfNeeded()
        val treeUrl = "/repos/$owner/$repo/git/trees"
        val requestBody = Gson().toJson(
            mapOf("base_tree" to baseTreeSha, "tree" to treeEntries)
        )
        val request = buildRequest(treeUrl, HttpMethod.Post, requestBody)
        val response = runBlocking { client.request(request).bodyAsText() }
        return Gson().fromJson(response, GHTree::class.java)
    }

    fun updateRef(owner: String, repo: String, ref: String, newSha: String, force: Boolean) {
        refreshTokenIfNeeded()
        val refUrl = "/repos/$owner/$repo/git/refs/$ref"
        val requestBody = Gson().toJson(mapOf("sha" to newSha, "force" to force))
        val request = buildRequest(refUrl, HttpMethod.Patch, requestBody)
        val response = runBlocking { client.request(request) }
        // You may want to handle the response here
    }

    private fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > tokenExpirationTime) {
            // Assume you have a method to refresh JWT token and get its expiration time
            val (newExpirationTime, newJwtToken) = fetchAccessToken()
            accessToken = newJwtToken
            tokenExpirationTime = newExpirationTime
            ghRepository = getGHRepository()
        }
    }

    fun createPullRequest(
        title: String, body: String, head: String, base: String
    ) {
        refreshTokenIfNeeded()
        val owner = ghRepository.ownerName
        val repo = ghRepository.name

        val apiUrl = "/repos/$owner/$repo/pulls"
        val messageBody = "{\"title\":\"$title\",\"body\":\"$body\",\"head\":\"$head\",\"base\":\"$base\"}"
        val request = buildRequest(apiUrl, HttpMethod.Post, messageBody)
        val response = runBlocking { client.request(request) }

        if (!response.status.isSuccess()) {
            println("Error creating pull request: ${response.status}, ${runBlocking { response.bodyAsText() }}")
        } else {
            println("Created pull request")
        }
    }

    fun createBranch(
        newBranchName: String, shaToBranchFrom: String
    ) {
        refreshTokenIfNeeded()
        val owner = ghRepository.ownerName
        val repo = ghRepository.name
        val gitRefUrl = "/repos/$owner/$repo/git/refs"
        val requestBody = "{\"ref\":\"refs/heads/${newBranchName}\",\"sha\":\"$shaToBranchFrom\"}"
        val request = buildRequest(gitRefUrl, HttpMethod.Post, requestBody)
        val response = runBlocking { client.request(request).bodyAsText() }
    }

    fun getBranches(
    ): List<Branch> {
        refreshTokenIfNeeded()
        val owner = ghRepository.ownerName
        val repo = ghRepository.name
        val branchesUrl = "/repos/$owner/$repo/branches"
        val request = buildRequest(branchesUrl, HttpMethod.Get, accessToken)
        val response = runBlocking { client.request(request).bodyAsText() }

        return Gson().fromJson(response, Array<Branch>::class.java).toList()
    }

    fun cloneRepo(outputDir: String): String? {
        refreshTokenIfNeeded()
        val owner = ghRepository.ownerName
        val repo = ghRepository.name
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

    fun fetchAccessToken(): Pair<Long, String> {
        val tokenUrl = "$baseUrl/app/installations/$installationId/access_tokens"
        val (expireDate, jwtToken) = createJwtToken(appId, algorithm)
        val accessTokenResponse = getAccessToken(tokenUrl, jwtToken)

        return expireDate to accessTokenResponse.token
    }

    private fun createJwtToken(appId: String, algorithm: Algorithm): Pair<Long, String> {
        val expireDate = System.currentTimeMillis() + 500000
        val token = JWT.create().withIssuer(appId).withIssuedAt(Date(System.currentTimeMillis() - 500000))
            .withExpiresAt(Date(expireDate)).sign(algorithm)

        return expireDate to token
    }

    suspend fun createCommit(
        message: String,
        treeSha: String,
        parentCommitSha: String,
    ): Commit {
        refreshTokenIfNeeded()
        val owner = ghRepository.ownerName
        val repo = ghRepository.name
        val commitUrl = "/repos/$owner/$repo/git/commits"
        val requestBody = Gson().toJson(
            mapOf(
                "message" to message, "tree" to treeSha, "parents" to listOf(parentCommitSha)
            )
        )
        val request = buildRequest(commitUrl, HttpMethod.Post, requestBody)
        val response = client.request(request)
        return Gson().fromJson(response.bodyAsText(), Commit::class.java)
    }

    fun setAccessToken(accessToken: String) {
        this.accessToken = accessToken
        this.ghRepository = getGHRepository()
    }

    suspend fun getLatestCommitSha(owner: String, repo: String, newBranchName: String): String {
        val refUrl = "/repos/$owner/$repo/git/ref/heads/$newBranchName"
        val request = buildRequest(refUrl, HttpMethod.Get)
        val response = client.request(request)
        val ref = Gson().fromJson(response.bodyAsText(), GHRef::class.java)
        return ref.getObject().sha
    }

    fun getTree(commitSha : String): GHTree {
        refreshTokenIfNeeded()
        return ghRepository.getTree(commitSha)
    }

    private fun getGHRepository() : GHRepository {
        val github = Utils.createGitHubClient(accessToken)
        return github.getRepository(repository.full_name)
    }

    fun createTree() : GHTreeBuilder {
        refreshTokenIfNeeded()
        return ghRepository.createTree()
    }

    fun getDefaultBranch(): String {
        refreshTokenIfNeeded()
        return ghRepository.defaultBranch
    }

}