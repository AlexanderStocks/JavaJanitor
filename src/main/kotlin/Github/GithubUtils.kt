package Github

import Utils.Utils.javaFileToBase64
import org.kohsuke.github.GHRepository
import refactor.RefactorService
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.system.exitProcess

class GithubUtils(private val githubAPI: GithubAPI, private val installationAccessToken: String) {
    fun processRepo(originalRepo: GHRepository) {
        val branches = githubAPI.getBranches(originalRepo.ownerName, originalRepo.name, installationAccessToken)
        val mainBranch = branches.find { it.name == "main" } ?: run {
            println("Main branch not found.")
            exitProcess(-1)
        }

        val newBranchName = "RefactoringJanitor"
        githubAPI.createBranch(
            originalRepo.ownerName, originalRepo.name, newBranchName, mainBranch.commit.sha, installationAccessToken
        )

        val repoName = githubAPI.cloneRepo(
            installationAccessToken, originalRepo.ownerName, originalRepo.name, "src/main/resources"
        )
        val repoPath = "C:\\Users\\Stock\\Desktop\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}"
        println("cloned at $repoPath")

        val refactoringService = RefactorService(repoPath)
        val modifiedFiles = refactoringService.refactor()
        val refactoringCount = mutableMapOf<String, Int>()

        modifiedFiles.forEach { (modifiedFile, refactorings) ->
            updateFileContent(
                repoPath, modifiedFile, refactorings, originalRepo, newBranchName, refactoringCount
            )
        }

        createPullRequest(
            originalRepo, newBranchName, modifiedFiles.size, refactoringCount
        )

        cleanUpRepo(repoPath)
    }

    private fun updateFileContent(
        repoPath: String,
        modifiedFile: Path,
        refactorings: List<String>,
        originalRepo: GHRepository,
        newBranchName: String,
        refactoringCount: MutableMap<String, Int>
    ) {
        val modifiedFileRelativePath = Path(repoPath).relativize(modifiedFile).toString().replace("\\", "/")
        val contents = githubAPI.getFileContent(
            installationAccessToken, originalRepo.ownerName, originalRepo.name, modifiedFileRelativePath
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

    private fun createPullRequest(
        originalRepo: GHRepository,
        newBranchName: String,
        numberOfModifiedFiles: Int,
        refactoringCount: Map<String, Int>
    ) {
        val prTitle = "Refactoring Janitor"
        val prBody = buildString {
            append("This pull request contains $numberOfModifiedFiles refactored files. ")
            refactoringCount.forEach { (refactoring, count) ->
                append("$refactoring, $count files. ")
            }
            append("Please review the changes.")
        }

        githubAPI.createPullRequest(
            installationAccessToken, originalRepo.ownerName, originalRepo.name, prTitle, prBody, newBranchName, "main"
        )
    }

    private fun cleanUpRepo(repoPath: String) {
        Files.walk(Path(repoPath)).forEach { file ->
            file.deleteIfExists()
        }
    }

}