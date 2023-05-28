import builder.BuildRunner
import tester.TestRunner
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val javaProjectPath = "C:\\Users\\Stock\\IdeaProjects\\JavaJanitor\\src\\main\\resources\\AlexanderStocks-Java-36232a8" // Replace with the path to your Java project

    val testRunner = BuildRunner.create(javaProjectPath)


    println("Running tests after modifying the code to make the test pass...")
    val testResultsPass = testRunner.buildProject()
    println(testResultsPass)
}
