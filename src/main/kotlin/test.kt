import refactor.RefactorService

fun main() {
    repoPath =
        "C:\\Users\\Stock\\Desktop\\JavaJanitor\\src\\main\\resources\\AlexanderStocks-Test-IncreaseCyclomaticComplexityByCommit-159322768db87ba195ea5ed70158c956866f4057"
    println("cloned at $repoPath")

    val refactoringService = RefactorService(repoPath)
    val modifiedFiles = refactoringService.refactor()

    val refactoringCount = mutableMapOf<String, Int>()

    modifiedFiles.forEach { (modifiedFile, refactorings) ->
        println("Modified file: $modifiedFile")
    }
}