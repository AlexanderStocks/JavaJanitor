package github

import org.kohsuke.github.GHRepository
import refactor.RefactorService
import utils.Utils.javaFileToBase64
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.system.exitProcess

class GithubUtils(private val githubAPI: GithubAPI, private val installationAccessToken: String) {
    fun processRepository(ghRepository: GHRepository) {
        println("Processing ${ghRepository.fullName}...")
        val newBranchName = setBranchName(ghRepository)
        println("branchName: $newBranchName")

        val repoPath = setupRepo(ghRepository, newBranchName)
        println("setupRepo: $repoPath")
        val refactoringService = RefactorService(repoPath)
        println("Refactoring ${ghRepository.fullName}...")
        val modifiedFiles = refactoringService.refactor()
        println("Refactored ${modifiedFiles.size} files.")
        val refactoringCount = uploadModifiedFiles(repoPath, ghRepository, modifiedFiles, newBranchName)
        println("Created pull request with $refactoringCount refactorings.")
        createPullRequest(ghRepository, newBranchName, modifiedFiles.size, refactoringCount)
        cleanUpRepo(repoPath)
    }

    private fun setBranchName(ghRepository: GHRepository): String {
        val baseBranchName = "refactoringJanitor"
        var newBranchName = baseBranchName
        var i = 1

        // If the branch already exists, append a number to the end.
        try {
            ghRepository.getBranch(newBranchName)
        } catch (e: Exception) {
            println("Branch $newBranchName does not exist.")
        }
//        while (ghRepository.getBranch(newBranchName) != null) {
//            newBranchName = "$baseBranchName${i++}"
//        }

        return newBranchName
    }

    private fun setupRepo(ghRepository: GHRepository, newBranchName: String): String {
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

        return "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}"
    }

    private fun uploadModifiedFiles(
        repoPath: String, ghRepository: GHRepository, modifiedFiles: Map<Path, List<String>>, newBranchName: String
    ): Map<String, Int> {
        val refactoringCount = mutableMapOf<String, Int>()

        modifiedFiles.forEach { (modifiedFile, refactorings) ->
            updateFileContent(
                repoPath, modifiedFile, refactorings, ghRepository, newBranchName, refactoringCount
            )
        }
        return refactoringCount
    }

    private fun updateFileContent(
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
            installationAccessToken, ghRepository.ownerName, ghRepository.name, prTitle, prBody, newBranchName, "main"
        )
    }

    private fun cleanUpRepo(repoPath: String) {
        Files.walk(Path(repoPath)).forEach { file ->
            file.deleteIfExists()
        }
    }
}