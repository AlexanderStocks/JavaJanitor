import refactor.RefactorService

fun main() {
    repoPath =
        "C:\\Users\\Stock\\Desktop\\JavaJanitor\\src\\main\\resources\\TestCases"
    println("cloned at $repoPath")

    val refactoringService = RefactorService(repoPath)
    val modifiedFiles = refactoringService.refactor()

    val refactoringCount = mutableMapOf<String, Int>()

    modifiedFiles.forEach { (modifiedFile, refactorings) ->
        println("Modified file: $modifiedFile")
    }
}