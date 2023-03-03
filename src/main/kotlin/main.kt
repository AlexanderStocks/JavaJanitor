
import App.Companion.createGitHubClient

import App.Companion.getRelativePathToParentDirectory
import App.Companion.javaFileToBase64
import App.Companion.loadPrivateKey
import App.Companion.parseWebhookPayload
import Github.CredentialsLoader
import Github.GithubAPI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.kohsuke.github.GHRepository
import java.io.File
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
            val githubAPI = GithubAPI()
            val installationAccessToken = githubAPI.fetchAccessToken(baseUrl, appId, algorithm, payload.installation.id)
            val github = createGitHubClient(installationAccessToken)
            val originalRepo = github.getRepository(payload.repositories.first().full_name)
            val branches = githubAPI.getBranches(originalRepo.ownerName, originalRepo.name, installationAccessToken)

            val mainBranch = branches.find { it.name == "main" }
            if (mainBranch != null) {
                println("Commit SHA for main branch: ${mainBranch.commit.sha}")
            } else {
                println("Main branch not found.")
                exitProcess(-1)
            }
            val newBranchName = "RefactoringJanitor"

            githubAPI.createBranch(originalRepo.ownerName, originalRepo.name, newBranchName, mainBranch.commit.sha, installationAccessToken)

            val repoName = githubAPI.cloneRepo(installationAccessToken, baseUrl, originalRepo.ownerName, originalRepo.name, "src/main/resources")
            val repoPath = "src/main/resources/${repoName?.removeSuffix(".zip")}"

            val refactoringService = RefactoringService(repoPath)

            val modifiedFiles = refactoringService.refactor()
            val modifiedFile = modifiedFiles.first()
            val modifiedFileRelativePath = getRelativePathToParentDirectory(modifiedFile.path, repoPath).replace("\\", "/")

            val contents = githubAPI.getFileContent(installationAccessToken, baseUrl, originalRepo.ownerName, originalRepo.name, modifiedFileRelativePath)
            val updateResponse = githubAPI.updateContent(originalRepo.ownerName, originalRepo.name, modifiedFileRelativePath, "Test commit", javaFileToBase64(modifiedFile), contents.sha, installationAccessToken, newBranchName)

            githubAPI.createPullRequest(installationAccessToken, baseUrl, originalRepo.ownerName, originalRepo.name, "Refactoring Janitor refactorings", "very nice changes", newBranchName, "main")

            //val forkResponse = forkRepository(originalRepo.ownerName, originalRepo.name, "RefactoringJanitor", true, installationAccessToken)

            File(repoPath).deleteRecursively()


            call.respond(HttpStatusCode.OK)
        }
    }
}





