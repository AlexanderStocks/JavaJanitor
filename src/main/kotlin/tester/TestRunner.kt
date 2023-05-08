package tester

import tester.testRunners.GradleTestRunner
import java.nio.file.Files
import java.nio.file.Path

interface TestRunner {
    fun runTests(): List<TestResult>

    companion object {
        fun create(projectLocation: String): TestRunner {
            val projectPath = Path.of(projectLocation)

            return when {
                Files.exists(projectPath.resolve("build.gradle")) -> GradleTestRunner(projectLocation)
                //Files.exists(projectPath.resolve("pom.xml")) -> MavenTestRunner(projectLocation)
                else -> throw UnsupportedOperationException("Unsupported build tool for the project at $projectLocation")
            }
        }
    }
}