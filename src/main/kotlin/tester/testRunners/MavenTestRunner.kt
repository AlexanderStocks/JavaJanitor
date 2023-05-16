package tester.testRunners

import tester.TestResult
import tester.TestRunner
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MavenTestRunner(private val projectLocation: String) : TestRunner {
    override fun runTests(): List<TestResult> {
        val builder = ProcessBuilder("mvn", "test")
        builder.directory(File(projectLocation))

        val process = builder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        val testResults = mutableListOf<TestResult>()

        reader.useLines { lines ->
            lines.filter { it.contains("[INFO] Tests run:") }.forEach { line ->
                val testName = extractTestName(line)
                val isSuccessful = !line.contains("FAILURE!")
                testResults.add(TestResult(testName, isSuccessful))
            }
        }

        process.waitFor()

        return testResults
    }

    private fun extractTestName(line: String): String {
        // Extract the test name from the line, assuming the line is in the format
        // [INFO] Running package.ClassName
        return line.substringAfter("[INFO] Running ").trim()
    }
}
