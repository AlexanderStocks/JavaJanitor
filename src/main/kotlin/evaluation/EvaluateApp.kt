package evaluation

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import metric.Model.Metric
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import refactor.RefactorService
import refactor.refactorings.removeDuplication.common.MethodMetrics
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

fun main() {
    val personalAccessToken = "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"
    val githubRepoUrl = "https://github.com/AlexanderStocks/AndroidSwipeLayout" // Enter your GitHub repo URL

//
//    val github = GitHub.connectUsingOAuth(personalAccessToken)
//    val searchResult = github.searchRepositories().q("language:java").sort(GHRepositorySearchBuilder.Sort.STARS).list()
//    val topTenRepos = searchResult.take(10)

    val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    val metricsCalculator = MethodMetrics()
    val localPath = "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\testProject"

    try {
        Git.cloneRepository()
            .setURI(githubRepoUrl)
            .setCredentialsProvider(UsernamePasswordCredentialsProvider("", personalAccessToken))
            .setDirectory(File(localPath))
            .call()
    } catch (e: GitAPIException) {
        println("Failed to clone the repository: $e")
    }

//    topTenRepos.forEach { repo ->
//        val forkedRepo = repo.fork()
//        val localPath = "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\evaluationRepos\\${forkedRepo.name}"
//
//        Git.cloneRepository()
//            .setURI(forkedRepo.sshUrl)
//            .setDirectory(File(localPath))
//            .call()
//
//        println("Name: ${repo.name}, Full Name: ${repo.fullName}, Size: ${repo.size}, URL: ${repo.htmlUrl}")

        val refactoringService = RefactorService(localPath)
        val methodsBeforeRefactoring = getMethods(localPath)
        val metricsBeforeRefactoring = methodsBeforeRefactoring.map { metricsCalculator.process(it) }
        val avgMetricsBeforeRefactoring = averageMetrics(metricsBeforeRefactoring)

        // Machine usage before refactoring
        val cpuUsageBefore = operatingSystemMXBean.systemLoadAverage

        // Refactor
        val modifiedFiles = refactoringService.refactor()

        val methodsAfterRefactoring = getMethods(localPath)
        val metricsAfterRefactoring = methodsAfterRefactoring.map {  metricsCalculator.process(it) }
        val avgMetricsAfterRefactoring = averageMetrics(metricsAfterRefactoring)

        // Machine usage after refactoring
        val cpuUsageAfter = operatingSystemMXBean.systemLoadAverage

    println("CPU usage before refactoring: $cpuUsageBefore")
    println("CPU usage after refactoring: $cpuUsageAfter")
    println("Average method metrics before refactoring: $avgMetricsBeforeRefactoring")
    println("Average method metrics after refactoring: $avgMetricsAfterRefactoring")

//    }


}

fun getMethods(localPath:String) : List<MethodDeclaration> {
    return Files.walk(Paths.get(localPath))
        .filter { path -> path.toString().endsWith(".java") }
        .map { path -> StaticJavaParser.parse(path) }
        .collect(Collectors.toList()).flatMap { it.findAll(MethodDeclaration::class.java) }
}

fun averageMetrics(metricsList: List<Map<Metric, Double>>): Map<Metric, Double> {
    val result = mutableMapOf<Metric, Double>()

    for (metrics in metricsList) {
        for ((metric, value) in metrics) {
            result[metric] = result.getOrDefault(metric, 0.0) + value
        }
    }

    for (metric in result.keys) {
        result[metric] = result[metric]!! / metricsList.size
    }

    return result
}