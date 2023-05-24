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
    val server = embeddedServer(Netty, port = 4567, module = Application::listenToGithubApp)
    server.start(wait = true)
}

fun Application.listenToGithubApp() {
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
            val githubAPI = GithubAPI()
            println("Received event: $eventType, payload: $payload")

            if (payload is PushEvent && payload.pusher.name == "refactoringjanitor[bot]") {
                println("Event pushed by the bot itself. Ignoring.")
                call.respond(HttpStatusCode.OK)
                return@post
            }

            //Check if the event is a branch deletion
            if (payload is PushEvent && payload.ref.startsWith("refs/heads/") && payload.deleted) {
                println("Branch deletion event. Ignoring.")
                call.respond(HttpStatusCode.OK)
                return@post
            }

            payload?.let { it1 -> Utils.getReposWithIds(it1) }?.forEach { repoWithId ->
                val (repository, installationId) = repoWithId
                println("Processing repository: ${repository.full_name}, installationId: $installationId")
                val installationAccessToken = githubAPI.fetchAccessToken(baseUrl, appId, algorithm, installationId)
                println("installationAccessToken: $installationAccessToken")
                githubAPI.setAccessToken(installationAccessToken)
                val github = createGitHubClient(installationAccessToken)
                println("github: $github")
                val githubRepo = github.getRepository(repository.full_name)
                println("githubRepo: $githubRepo")

                GithubUtils(githubAPI, installationAccessToken).processRepository(githubRepo)
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}