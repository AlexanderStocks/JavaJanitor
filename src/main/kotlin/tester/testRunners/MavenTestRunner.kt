package tester.testRunners

import tester.TestResult
import tester.TestRunner
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MavenTestRunner(private val projectLocation: String) : TestRunner {
    override fun runTests(): List<TestResult> {
        val testResults = mutableListOf<TestResult>()

        try {
            val mavenHome =
                "C:\\Users\\Stock\\Downloads\\apache-maven-3.9.2-bin\\apache-maven-3.9.2" // Specify the path to your Maven installation

            val command = "cmd /c ${mavenHome}\\bin\\mvn.cmd test"
            val process = Runtime.getRuntime().exec(command, null, File(projectLocation))

            val reader = BufferedReader(InputStreamReader(process.inputStream))


            var testName = ""

            reader.useLines { lines ->
                lines.forEach { line ->

                    if (line.contains("INFO] Running ")) {
                        testName = extractTestName(line)
                    } else if (line.contains("INFO] Tests run:")) {
                        val testsRun = extractNumberAfterPrefix(line, "Tests run:")
                        val failures = extractNumberAfterPrefix(line, "Failures:")
                        val errors = extractNumberAfterPrefix(line, "Errors:")
                        val skipped = extractNumberAfterPrefix(line, "Skipped:")

                        testResults.add(TestResult(testName, testsRun, failures, errors, skipped))
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
        // [INFO] Running package.ClassName
        return line.substringAfter("[INFO] Running ").trim()
    }

    private fun extractNumberAfterPrefix(line: String, prefix: String): Int {
        return line.substringAfter(prefix).substringBefore(",").trim().toInt()
    }
}
