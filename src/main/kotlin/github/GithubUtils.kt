package github

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.kohsuke.github.GHRepository
import refactor.RefactorService
import utils.Utils.javaFileToBase64
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.system.exitProcess

class GithubUtils(private val githubAPI: GithubAPI, private val installationAccessToken: String) {
    suspend fun processRepository(ghRepository: GHRepository) {
        println("Processing ${ghRepository.fullName}...")
        coroutineScope { // Create a custom scope for the repository
            val newBranchName = setBranchName(ghRepository)
            println("branchName: $newBranchName")

            val (repoPath, baseBranchName) = setupRepo(ghRepository, newBranchName)
            println("setupRepo: $repoPath")

            val refactoringService = RefactorService(repoPath)
            println("Refactoring ${ghRepository.fullName}...")
            val modifiedFiles = refactoringService.refactor()
            println("Refactored ${modifiedFiles.size} files.")
            val refactoringCount = uploadModifiedFiles(repoPath, ghRepository, modifiedFiles, newBranchName)
            println("Created pull request with $refactoringCount refactorings.")

            createPullRequest(ghRepository, newBranchName, baseBranchName, modifiedFiles.size, refactoringCount)

            cleanUpRepo(repoPath)
        }
    }

    private fun setBranchName(ghRepository: GHRepository): String {
        val baseBranchName = "refactoringJanitor"
        var newBranchName = baseBranchName
        var i = 1
        val repoBranches = ghRepository.branches


        while (repoBranches.containsKey(newBranchName)) {
            newBranchName = "$baseBranchName${i++}"
        }

        return newBranchName
    }

    private fun setupRepo(ghRepository: GHRepository, newBranchName: String): Pair<String, String> {
        val branches = githubAPI.getBranches(ghRepository.ownerName, ghRepository.name, installationAccessToken)
        println("branches: $branches")
        val mainBranch = branches.find { it.name == "main" || it.name == "master" } ?: run {
            println("Main branch not found.")
            exitProcess(-1)
        }

        githubAPI.createBranch(
            ghRepository.ownerName, ghRepository.name, newBranchName, mainBranch.commit.sha, installationAccessToken
        )

        val repoName = githubAPI.cloneRepo(
            installationAccessToken, ghRepository.ownerName, ghRepository.name, "src/main/resources"
        )
        println("Cloned repo to $repoName")

        return Pair(
            "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}",
            mainBranch.name
        )
    }

    private suspend fun uploadModifiedFiles(
        repoPath: String, ghRepository: GHRepository, modifiedFiles: Map<Path, List<String>>, newBranchName: String
    ): Map<String, Int> = coroutineScope {
        val refactoringCount = mutableMapOf<String, Int>()

        modifiedFiles.map { (modifiedFile, refactorings) ->
            async {
                updateFileContent(
                    repoPath, modifiedFile, refactorings, ghRepository, newBranchName, refactoringCount
                )
            }
        }.forEach { it.await() }


        refactoringCount
    }


    private suspend fun updateFileContent(
        repoPath: String,
        modifiedFile: Path,
        refactorings: List<String>,
        ghRepository: GHRepository,
        newBranchName: String,
        refactoringCount: MutableMap<String, Int>
    ) {
        val modifiedFileRelativePath = Path(repoPath).relativize(modifiedFile).toString().replace("\\", "/")
        val contents = githubAPI.getFileContent(
            installationAccessToken, ghRepository.ownerName, ghRepository.name, modifiedFileRelativePath
        )

        val commitMessage = buildString {
            append("Refactor: ")
            refactorings.forEachIndexed { index, refactoring ->
                append(refactoring)
                refactoringCount[refactoring] = refactoringCount.getOrDefault(refactoring, 0) + 1

                if (index < refactorings.size - 1) {
                    append(", ")
                } else {
                    append(".")
                }
            }
        }


        githubAPI.updateContent(
            ghRepository.ownerName,
            ghRepository.name,
            modifiedFileRelativePath,
            commitMessage,
            javaFileToBase64(modifiedFile.toFile()),
            contents.sha,
            installationAccessToken,
            newBranchName
        )
    }

    private fun createPullRequest(
        ghRepository: GHRepository,
        newBranchName: String,
        baseBranchName: String,
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
            installationAccessToken,
            ghRepository.ownerName,
            ghRepository.name,
            prTitle,
            prBody,
            newBranchName,
            baseBranchName
        )
    }

    private fun cleanUpRepo(repoPath: String) {
        Files.walk(Path(repoPath)).forEach { file ->
            file.deleteIfExists()
        }
    }
}