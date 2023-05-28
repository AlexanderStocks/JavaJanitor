import com.github.javaparser.StaticJavaParser
import refactor.RefactorService
import refactor.refactorings.removeDuplication.RemoveDuplication
import refactor.refactorings.removeDuplication.type1Clones.Type1CloneFinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.nio.file.Path

fun main() {

    val repoPath =
        "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\type3Tests"
//    val repoPath = "C:\\Users\\Stock\\Desktop\\JavaProject"

    val cus = Files.walk(Paths.get(repoPath)).filter { path -> path.toString().endsWith(".java") }.map { path ->
        try {
            StaticJavaParser.parse(path)
        } catch (e: Exception) {
            println("Failed to parse $path")
            null
        }
    }.collect(Collectors.toList()).filterNotNull()

    val refactoringService = RefactorService(Paths.get(repoPath))
    val modifiedFiles = RemoveDuplication().process(Paths.get(repoPath), cus)

    val seen = hashSetOf<Path>()
    modifiedFiles.map { it.storage.get().path to it }.forEach {
        if (it.first !in seen) {
            seen.add(it.first)
            println("filepath: ${it.first}, contents: ${it.second.toString()}")
        }
    }

//    modifiedFiles.forEach { modifiedFile ->
//        println("Modified file: $modifiedFile")
//        val content = Files.readString(modifiedFile)
//        println("Content: $content")

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

fun getSystemCpuLoad(): String {
    return try {
        val process = Runtime.getRuntime().exec("wmic cpu get loadpercentage")
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLine() // This will get the header: LoadPercentage
        reader.readLine().trim() // This will get the actual CPU usage
    } catch (e: Exception) {
        "-1"
    }
}