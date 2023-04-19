//import refactoring.RefactoringService
import Github.CredentialsLoader
import Github.GithubAPI
import Utils.Companion.createGitHubClient
import Utils.Companion.javaFileToBase64
import Utils.Companion.loadPrivateKey
import Utils.Companion.parseWebhookPayload
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import refactor.RefactorService
import java.nio.file.Files
import java.security.Security
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.system.exitProcess

var repoPath = ""

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
            val githubAPI = GithubAPI()

            val installationAccessToken = githubAPI.fetchAccessToken(baseUrl, appId, algorithm, payload.installation.id)
            println("Token $installationAccessToken")

            val github = createGitHubClient(installationAccessToken)
            println("Created client")

            val originalRepo = github.getRepository(payload.repositories.first().full_name)
            println("got repo")

            val branches = githubAPI.getBranches(originalRepo.ownerName, originalRepo.name, installationAccessToken)
            println("got branches")

            val mainBranch = branches.find { it.name == "main" }
            if (mainBranch != null) {
                println("Commit SHA for main branch: ${mainBranch.commit.sha}")
            } else {
                println("Main branch not found.")
                exitProcess(-1)
            }
            val newBranchName = "RefactoringJanitor"

            githubAPI.createBranch(
                originalRepo.ownerName,
                originalRepo.name,
                newBranchName,
                mainBranch.commit.sha,
                installationAccessToken
            )
            val repoName = githubAPI.cloneRepo(
                installationAccessToken,
                originalRepo.ownerName,
                originalRepo.name,
                "src/main/resources"
            )

            repoPath = "C:\\Users\\Stock\\Desktop\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}"
            println("cloned at $repoPath")

            val refactoringService = RefactorService(repoPath)
            println("Started refactor service")

            val modifiedFiles = refactoringService.refactor()
            val refactoringCount = mutableMapOf<String, Int>()

            modifiedFiles.forEach { (modifiedFile, refactorings) ->
                val modifiedFileRelativePath = Path(repoPath).relativize(modifiedFile).toString().replace("\\", "/")
                val contents = githubAPI.getFileContent(
                    installationAccessToken,
                    originalRepo.ownerName,
                    originalRepo.name,
                    modifiedFileRelativePath
                )


                val commitMessage = buildString {
                    append("Refactor:\n")
                    refactorings.forEach { refactoring ->
                        append("- $refactoring\n")
                        refactoringCount[refactoring] = refactoringCount.getOrDefault(refactoring, 0) + 1

                    }
                }

                println("Commit message is $commitMessage")

                githubAPI.updateContent(
                    originalRepo.ownerName,
                    originalRepo.name,
                    modifiedFileRelativePath,
                    "commitMessage",
                    javaFileToBase64(modifiedFile.toFile()),
                    contents.sha,
                    installationAccessToken,
                    newBranchName
                )
            }

//            val modifiedFileRelativePath =
//                getRelativePathToParentDirectory(modifiedFile.path, repoPath).replace("\\", "/")
//
//            val contents = githubAPI.getFileContent(
//                installationAccessToken,
//                originalRepo.ownerName,
//                originalRepo.name,
//                modifiedFileRelativePath
//            )
//
//            githubAPI.updateContent(
//                originalRepo.ownerName,
//                originalRepo.name,
//                modifiedFileRelativePath,
//                "Refactor",
//                javaFileToBase64(modifiedFile),
//                contents.sha,
//                installationAccessToken,
//                newBranchName
//            )
//
            val prTitle = "Refactoring Janitor"
            val prBody = buildString {
                append("This pull request contains ${modifiedFiles.size} refactored files. ")
                refactoringCount.forEach { (refactoring, count) ->
                    append("$refactoring, $count files. ")
                }
                append("Please review the changes.")
            }

            println(prBody)


            githubAPI.createPullRequest(
                installationAccessToken,
                originalRepo.ownerName,
                originalRepo.name,
                "prTitle",
                "prBody",
                newBranchName,
                "main"
            )

            Files.walk(Path(repoPath)).forEach { file ->
                file.deleteIfExists()
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}





