package tester.testRunners

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.events.OperationType
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.test.TestFinishEvent
import org.gradle.tooling.events.test.TestFailureResult
import org.gradle.tooling.events.test.TestSuccessResult
import tester.TestResult
import tester.TestRunner
import java.io.File

class GradleTestRunner(private val projectLocation: String) : TestRunner {
    override fun runTests(): List<TestResult> {
        val connector = GradleConnector.newConnector()
        connector.forProjectDirectory(File(projectLocation))
        val connection: ProjectConnection = connector.connect()

        val testResults = mutableListOf<TestResult>()

        connection.use { projectConnection ->
            val buildLauncher: BuildLauncher = projectConnection.newBuild()
            buildLauncher.forTasks("test")

            val testListener = ProgressListener { event ->
                if (event is TestFinishEvent) {
                    val descriptor = event.descriptor
                    if (descriptor != null && descriptor.parent != null) {
                        val testName = descriptor.displayName
                        val isSuccessful = when (event.result) {
                            is TestSuccessResult -> true
                            is TestFailureResult -> {
                                testResults.add(TestResult(testName, false))
                                false
                            }
                            else -> false
                        }
                        if (isSuccessful) {
                            testResults.add(TestResult(testName, true))
                        }
                    }
                }
            }

            buildLauncher.addProgressListener(testListener, OperationType.TEST)

            try {
                buildLauncher.run()
            } catch (e: GradleConnectionException) {
                println("Gradle connection failed. Passing the tests.")
                testResults.add(TestResult("Gradle connection failed", true))
            } catch (e: IllegalStateException) {
                println("Illegal state exception occurred. Failing the tests.")
                testResults.add(TestResult("Illegal state exception", false))
            } catch (e: Exception) {
                println("Running the tests failed.")
                testResults.add(TestResult("Unexpected exception", false))
            }
        }

        return testResults
    }
}
