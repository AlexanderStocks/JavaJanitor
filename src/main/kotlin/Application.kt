import config.CredentialsLoader
import github.GithubAPI
import github.GithubUtils
import github.apiFormats.push.PushEvent
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import utils.Utils
import utils.Utils.createGitHubClient
import java.security.Security

fun main() {
    val server = embeddedServer(Netty, port = 4567, module = Application::ListenToGithubApp)
    server.start(wait = true)
}

fun Application.ListenToGithubApp() {
    val config = CredentialsLoader("config/config.yml").load()
    val appId = config["GITHUB_APP_ID"] as String
    val baseUrl = config["GITHUB_BASE_URL"] as String
    val privateKeyPath = config["GITHUB_APP_PRIVATE_KEY_PATH"] as String

    Security.addProvider(BouncyCastleProvider())
    val algorithm = Utils.loadPrivateKey(privateKeyPath)

    routing {
        post("/") {
            val body = call.receiveText()
            val eventType = call.request.headers["X-GitHub-Event"]
            val payload = Utils.parseWebhookPayload(body, eventType ?: "")

            if (payload is PushEvent && payload.pusher.name == "refactoringjanitor[bot]") {
                println("Event pushed by the bot itself. Ignoring.")
                call.respond(HttpStatusCode.OK)
                return@post
            }

            payload?.let { it1 -> Utils.getReposWithIds(it1) }?.forEach { repoWithId ->
                val repository = repoWithId.first
                val installationId = repoWithId.second
                val githubAPI = GithubAPI()
                val installationAccessToken = githubAPI.fetchAccessToken(baseUrl, appId, algorithm, installationId)
                val github = createGitHubClient(installationAccessToken)
                val originalRepo = github.getRepository(repository.full_name)
                val githubUtils = GithubUtils(githubAPI, installationAccessToken)

                githubUtils.processRepo(originalRepo)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}