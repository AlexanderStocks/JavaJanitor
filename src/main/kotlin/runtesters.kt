import tester.TestRunner
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val javaProjectPath = "C:\\Users\\Stock\\Desktop\\JavaProject" // Replace with the path to your Java project

    val testRunner = TestRunner.create(javaProjectPath)

    // Modify the code to make the test pass
    val sourceFilePath = Paths.get(javaProjectPath, "src", "main", "java", "com", "example", "MyClass.java")
    val sourceCode = """
        package com.example;

        public class MyClass {
            public static int add(int a, int b) {
                return a + b;
            }
        }
    """.trimIndent()
    Files.writeString(sourceFilePath, sourceCode)

    println("Running tests after modifying the code to make the test pass...")
    val testResultsPass = testRunner.runTests()
    testResultsPass.forEach { result ->
        println("Test: ${result.testName}, Result: ${if (result.isSuccessful) "PASSED" else "FAILED"}")
    }

    // Modify the code to make the test fail
    val sourceCodeFail = """
        package com.example;

        public class MyClass {
            public static int add(int a, int b) {
                return a - b; // This will cause the test to fail
            }
        }
    """.trimIndent()
    Files.writeString(sourceFilePath, sourceCodeFail)

    println("\nRunning tests after modifying the code to make the test fail...")
    try {
        val testResultsFail = testRunner.runTests()
        testResultsFail.forEach { result ->
            println("Test: ${result.testName}, Result: ${if (result.isSuccessful) "PASSED" else "FAILED"}")
        }
    } catch (e: Exception) {
        println("An exception occurred while running the tests: ${e.message}")
        // Handle the exception or report the test failure as needed
    }
}
