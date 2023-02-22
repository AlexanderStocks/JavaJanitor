import Github.AccessTokenResponse
import Github.InitialiseEvent
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.FileReader
import java.security.interfaces.RSAPrivateKey
import java.util.*
import kotlin.system.exitProcess

class App {
    companion object {
        fun loadPrivateKey(privateKeyPath: String): Algorithm {
            val pemParser = PEMParser(FileReader(privateKeyPath))
            val converter = JcaPEMKeyConverter().setProvider("BC")
            val readObject = pemParser.readObject() as PEMKeyPair
            val privateKey = converter.getPrivateKey(readObject.privateKeyInfo)
            return Algorithm.RSA256(null, privateKey as RSAPrivateKey)
        }

        fun parseWebhookPayload(body: String): InitialiseEvent {
            return try {
                Gson().fromJson(body, InitialiseEvent::class.java)
            } catch (e: Exception) {
                println("Failed to parse webhook: ${e.message}")
                exitProcess(1)
            }
        }

        fun fetchAccessToken(baseUrl: String, appId: String, algorithm: Algorithm, installationId: Int): String {
            val tokenUrl = "$baseUrl/app/installations/$installationId/access_tokens"
            val jwtToken = createJwtToken(appId, algorithm)
            val accessTokenResponse = getAccessToken(tokenUrl, jwtToken)
            return accessTokenResponse.token
        }

        fun createJwtToken(appId: String, algorithm: Algorithm): String {
            return JWT.create()
                .withIssuer(appId)
                .withIssuedAt(Date(System.currentTimeMillis() - 500000))
                .withExpiresAt(Date(System.currentTimeMillis() + 500000))
                .sign(algorithm)
        }

        fun getAccessToken(tokenUrl: String, jwtToken: String): AccessTokenResponse {
            val client = HttpClient(Apache)
            val httpRequestBuilder = HttpRequestBuilder()
            httpRequestBuilder.url(tokenUrl)
            httpRequestBuilder.method = HttpMethod.Post
            httpRequestBuilder.contentType(ContentType.Application.Json)
            httpRequestBuilder.header(HttpHeaders.Authorization, "Bearer $jwtToken")
            httpRequestBuilder.accept(ContentType.Application.Json)
            val body = runBlocking { client.request(httpRequestBuilder).bodyAsText() }
            return Gson().fromJson(body, AccessTokenResponse::class.java )
        }

        fun createGitHubClient(installationAccessToken: String): GitHub {
            return GitHubBuilder()
                .withJwtToken(installationAccessToken)
                .build()
        }

        fun createPullRequest(repo: GHRepository): GHPullRequest {
            val pullRequestTitle = "Automatic pull request"
            val pullRequestBody = "This pull request was created automatically from a GitHub webhook"
            return repo.createPullRequest(pullRequestTitle, "main", "main", pullRequestBody)
        }
    }
}