package evaluation

import org.eclipse.jgit.api.Git
import org.kohsuke.github.GHRepositorySearchBuilder
import org.kohsuke.github.GitHub
import refactor.RefactorService
import java.io.File

fun main () {
    val personalAccessToken = "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"

    val github = GitHub.connectUsingOAuth(personalAccessToken) // Replace with your GitHub personal access token
    val searchResult = github.searchRepositories().q("language:java").sort(GHRepositorySearchBuilder.Sort.STARS).list()
    val topTenRepos = searchResult.take(10)



    topTenRepos.forEach { repo ->
        val forkedRepo = repo.fork() // Fork the repository
        val localPath =
            "/path/to/your/local/directory/${forkedRepo.name}" // Replace with the local path where you want to clone the repositories

        Git.cloneRepository()
            .setURI(forkedRepo.sshUrl)
            .setDirectory(File(localPath))
            .call()

        println("Name: ${repo.name}, Full Name: ${repo.fullName}, Size: ${repo.size}, URL: ${repo.htmlUrl}")

        // Process the repository with RefactorService
        val refactoringService = RefactorService(localPath)
        val modifiedFiles = refactoringService.refactor()

    }
}