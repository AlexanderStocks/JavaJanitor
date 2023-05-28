package tester.testRunners

import tester.TestResult
import tester.TestRunner
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class GradleTestRunner(private val projectLocation: String) : TestRunner {
    override fun runTests(): List<TestResult> {
        val testResults = mutableListOf<TestResult>()

        try {
            val gradleHome = "C:\\Gradle\\gradle-8.1.1" // Specify the path to your Gradle installation

            val command = "cmd /c ${gradleHome}\\bin\\gradle.bat test"
            val process = Runtime.getRuntime().exec(command, null, File(projectLocation))

            val reader = BufferedReader(InputStreamReader(process.inputStream))


            var testName: String
            var testsRun = 0
            var failures = 0

            reader.useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.contains("Test run finished after") -> {
                            testsRun += 1
                        }

                        line.contains("Test run failed") -> {
                            failures += 1
                        }

                        line.contains("> Task :test") -> {
                            testName = line.substringAfter("> Task :test").trim()
                            testResults.add(TestResult(testName, testsRun, failures, 0, 0))
                            testsRun = 0
                            failures = 0
                        }
                    }
                }
            }

            process.waitFor()
        } catch (e: Exception) {
            println("Error: ${e.message}")
            testResults.add(TestResult("Errored", 0, 0, 1, 0))
        }

        return testResults
    }

    private fun extractTestName(line: String): String {
        // Extract the test name from the line, assuming the line is in the format
        // > Task :test --tests package.ClassName
        return line.substringAfter("--tests ").trim()
    }
}
