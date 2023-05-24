package github

import github.apiFormats.commit.TreeEntry
import kotlinx.coroutines.coroutineScope
import org.kohsuke.github.GHRepository
import refactor.RefactorService
import utils.Utils.javaFileToBase64
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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
            val refactoringMap = refactoringService.refactor(githubAPI, ghRepository, newBranchName)
            val refactoringCount = refactoringMap.map { (key, value) -> key to value.size }.toMap()
            val modifiedFiles = refactoringMap.keys.toSet()
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

    private fun setupRepo(ghRepository: GHRepository, newBranchName: String): Pair<Path, String> {
        val defaultBranchName = ghRepository.defaultBranch
        val branches = githubAPI.getBranches(ghRepository.ownerName, ghRepository.name)
        println("branches: $branches")
        val defaultBranch = branches.find { it.name == defaultBranchName } ?: run {
            println("Default branch not found.")
            exitProcess(-1)
        }

        githubAPI.createBranch(
            ghRepository.ownerName, ghRepository.name, newBranchName, defaultBranch.commit.sha
        )

        val repoName = githubAPI.cloneRepo(
            ghRepository.ownerName, ghRepository.name, "src/main/resources"
        )
        println("Cloned repo to $repoName")
        val repoPath =
            Paths.get("C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\${repoName?.removeSuffix(".zip")}")
        return Pair(
            repoPath,
            defaultBranch.name
        )
    }


    private suspend fun uploadModifiedFiles(
        repoPath: String,
        ghRepository: GHRepository,
        refactoringsToFiles: Map<String, List<Path>>,
        newBranchName: String
    ): Map<String, Int> = coroutineScope {
        val refactoringCount = mutableMapOf<String, Int>()

        refactoringsToFiles.forEach { (refactoring, files) ->
            // Create blobs for all the files modified by the refactoring
            val treeEntries = files.map { file ->
                val blob = githubAPI.createBlob(
                    ghRepository.ownerName,
                    ghRepository.name,
                    javaFileToBase64(file.toFile()),
                    installationAccessToken
                )
                TreeEntry(
                    path = file.toString(),
                    mode = "100644",
                    type = "blob",
                    sha = blob.sha,
                    size = null,
                    url = null
                )
            }

            // Create the tree that includes the blobs and references the base tree
            val tree = githubAPI.createTree(
                ghRepository.ownerName,
                ghRepository.name,
                treeEntries
            )

            // Create a new commit that points to this tree and specifies the parent commit
            val commit = githubAPI.createCommit(
                ghRepository.ownerName,
                ghRepository.name,
                "Refactor: $refactoring.",
                tree.sha, // tree.sha is used here
                newBranchName
            )

            // Update the reference to point to the new commit
            githubAPI.updateRef(
                ghRepository.ownerName,
                ghRepository.name,
                "heads/$newBranchName",
                commit.sha // commit.sha is used here
            )

            refactoringCount[refactoring] = files.size
            println("Created commit for refactoring: $refactoring affecting ${files.size} files.")
        }

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
            ghRepository.ownerName, ghRepository.name, modifiedFileRelativePath
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
            ghRepository.ownerName,
            ghRepository.name,
            prTitle,
            prBody,
            newBranchName,
            baseBranchName
        )
    }

    private fun cleanUpRepo(repoPath: Path) {
        Files.walk(repoPath).forEach { file ->
            file.deleteIfExists()
        }
    }
}