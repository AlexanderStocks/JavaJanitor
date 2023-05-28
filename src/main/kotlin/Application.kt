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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import utils.Utils
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

            if (payload is PushEvent && payload.pusher.name == "HardcodedCat") {
                // Event pushed by the bot itself. Ignoring.
                call.respond(HttpStatusCode.OK)
                return@post
            }

            if (payload is PushEvent && payload.pusher.name == "refactoringjanitor[bot]") {
                // Event pushed by the bot itself. Ignoring.
                call.respond(HttpStatusCode.OK)
                return@post
            }

            //Check if the event is a branch deletion
            if (payload is PushEvent && payload.ref.startsWith("refs/heads/") && payload.deleted) {
                // Branch deletion event. Ignoring.
                call.respond(HttpStatusCode.OK)
                return@post
            }

            println("Received event: $eventType")

            runBlocking {
                payload?.let { it1 -> Utils.getReposWithIds(it1) }?.map { repoWithId ->
                    launch(IO) {
                        try {
                            val processingTime = kotlin.system.measureTimeMillis {
                                val (repository, installationId) = repoWithId
                                println("Processing repository: ${repository.full_name}, installationId: $installationId")
                                val githubAPI = GithubAPI(baseUrl, appId, algorithm, installationId, repository)
                                val (_, installationAccessToken) = githubAPI.fetchAccessToken()
                                println("Installation access token: $installationAccessToken")
                                githubAPI.setAccessToken(installationAccessToken)
                                try {
                                    GithubUtils(githubAPI).processRepository()
                                } catch (e: Exception) {
                                    println("error is $e")
                                    e.printStackTrace()
                                    println("Error processing repository: ${repository.full_name}, installationId: $installationId")
                                }
                                println("Processed repository: ${repository.full_name}, installationId: $installationId")
                            }
                            print("in $processingTime ms")
                        } catch (e: Exception) {
                            println("error is $e")
                            println("Error processing repository: ${repoWithId.first.full_name}, installationId: ${repoWithId.second}")
                        }
                    }
                }?.forEach { it.join() }
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}