import App.Companion.createGitHubClient
import App.Companion.fetchAccessToken
import App.Companion.loadPrivateKey
import App.Companion.parseWebhookPayload
import Github.CredentialsLoader
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.kohsuke.github.GHRepository
import java.io.IOException
import java.security.Security
import kotlin.system.exitProcess


fun main() {
    val server = embeddedServer(Netty, port = 4567, module = Application::ListenToGithubApp)
    server.start(wait = true)
}

fun Application.ListenToGithubApp() {
    val config = CredentialsLoader("Config/config.yml").load()
    val appId = config["GITHUB_APP_ID"] as String
    val baseUrl = config["GITHUB_BASE_URL"] as String
    val privateKeyPath = config["GITHUB_APP_PRIVATE_KEY_PATH"] as String

    Security.addProvider(BouncyCastleProvider())
    val algorithm = loadPrivateKey(privateKeyPath)

    routing {
        post("/") {
            val body = call.receiveText()
            val payload = parseWebhookPayload(body)
            println("payload $payload")
            val installationAccessToken = fetchAccessToken(baseUrl, appId, algorithm, payload.installation.id)
            println("installationAccessToken $installationAccessToken")

            val github = createGitHubClient(installationAccessToken)
            println("github $github")

            val originalRepo = github.getRepository(payload.repositories.first().full_name)
            println("originalRepo $originalRepo")

            val forkedRepo :GHRepository

//            try {
//                forkedRepo = originalRepo.fork()
//
//            } catch (err: IOException) {
//                println("err $err")
//                exitProcess(1)
//            }
            println(originalRepo.fullName)
            val localRepo = Git("https://github.com/" + originalRepo.fullName)
            println("localRepo $localRepo")

            val refactoringService = RefactoringService(localRepo)
            refactoringService.refactor()

            //val pullRequest = createPullRequest()

            //localRepo.removeRepo()
            call.respondText("Received webhook: $payload")
        }
    }
}





