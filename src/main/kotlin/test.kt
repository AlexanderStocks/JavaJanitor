import refactor.RefactorService
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val repoPath =
        "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\AlexanderStocks-zuul-88186c0"
//    val repoPath = "C:\\Users\\Stock\\Desktop\\JavaProject"
    println("cloned at $repoPath")

    val refactoringService = RefactorService(Paths.get(repoPath))
//    val modifiedFiles = refactoringService.refactor()
//
//    val refactoringCount = mutableMapOf<String, Int>()
//
//    modifiedFiles.forEach { (modifiedFile, refactorings) ->
//        println("Modified file: $modifiedFile")
//        println("Refactorings applied: ${refactorings.joinToString(", ")}")
////        val content = Files.readString(modifiedFile)
////        println("Content: $content")
//    }
//
//    val originalMethod = StaticJavaParser.parseMethodDeclaration("""
//            public void test() {
//                int i = 0;
//                switch (i) {
//                    case 0:
//                        System.out.println("0");
//                        break;
//                    case 1:
//                        System.out.println("1");
//                        break;
//                    default:
//                        System.out.println("default");
//                }
//            }
//        """.trimIndent())
//
//
//    val convertedMethod = Type4CloneElementReplacer.replace(originalMethod)
//
//    println(convertedMethod)
}