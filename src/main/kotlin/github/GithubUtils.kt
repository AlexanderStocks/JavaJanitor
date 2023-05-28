package github

import kotlinx.coroutines.coroutineScope
import refactor.RefactorService
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.system.exitProcess

class GithubUtils(private val githubAPI: GithubAPI) {
    suspend fun processRepository() {
        coroutineScope { // Create a custom scope for the repository
            val newBranchName = setBranchName()
            val (repoPath, baseBranchName) = setupRepo(newBranchName)
            val refactoringService = RefactorService(repoPath)
            val refactoringMap = refactoringService.refactor(githubAPI, newBranchName)
            val refactoringCount = refactoringMap.map { (key, value) -> key to value.size }.toMap()
            val modifiedFiles = refactoringMap.keys.toSet()
            createPullRequest(newBranchName, baseBranchName, modifiedFiles.size, refactoringCount)

            cleanUpRepo(repoPath)
        }
    }

    private fun setBranchName(): String {
        val baseBranchName = "refactoringJanitor"
        var newBranchName = baseBranchName
        var i = 1
        val repoBranches = githubAPI.getBranches()
        while (repoBranches.any { it.name == newBranchName }) {
            newBranchName = "$baseBranchName${i++}"
        }

        return newBranchName
    }

    private fun setupRepo(newBranchName: String): Pair<Path, String> {
        val defaultBranchName = githubAPI.getDefaultBranch()
        val branches = githubAPI.getBranches()
        val defaultBranch = branches.find { it.name == defaultBranchName } ?: run {
            println("Default branch not found.")
            exitProcess(-1)
        }

        githubAPI.createBranch(
            newBranchName, defaultBranch.commit.sha
        )

        val repoName = githubAPI.cloneRepo(
            "src/main/resources"
        )
        val repoPath =
            Paths.get("C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}")
        return Pair(
            repoPath, defaultBranch.name
        )
    }

    private fun createPullRequest(
        newBranchName: String, baseBranchName: String, numberOfModifiedFiles: Int, refactoringCount: Map<String, Int>
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
            prTitle, prBody, newBranchName, baseBranchName
        )
    }

    private fun cleanUpRepo(repoPath: Path) {
        Files.walkFileTree(repoPath, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (exc == null) {
                    Files.delete(dir)
                    return FileVisitResult.CONTINUE
                } else {
                    throw exc
                }
            }
        })
    }
}