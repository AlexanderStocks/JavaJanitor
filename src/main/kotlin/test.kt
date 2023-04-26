import refactor.RefactorService

fun main() {
    val repoPath =
        "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\testCases"
    println("cloned at $repoPath")

    val refactoringService = RefactorService(repoPath)
    val modifiedFiles = refactoringService.refactor()

    val refactoringCount = mutableMapOf<String, Int>()

    modifiedFiles.forEach { (modifiedFile, refactorings) ->
        println("Modified file: $modifiedFile")
    }
}