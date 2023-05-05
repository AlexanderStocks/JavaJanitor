import refactor.RefactorService
import java.nio.file.Files

fun main() {
    val repoPath =
        "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\type3Tests"
    println("cloned at $repoPath")

    val refactoringService = RefactorService(repoPath)
    val modifiedFiles = refactoringService.refactor()

    val refactoringCount = mutableMapOf<String, Int>()

    modifiedFiles.forEach { (modifiedFile, refactorings) ->
        println("Modified file: $modifiedFile")
        println("Refactorings applied: ${refactorings.joinToString(", ")}")
        val content = Files.readString(modifiedFile)
        println("Content: $content")
    }
}