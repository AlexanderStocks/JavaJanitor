package tester.testRunners

import tester.TestResult
import tester.TestRunner

class EmptyTestRunner : TestRunner{
    override fun runTests(): List<TestResult> {
        return listOf(TestResult("NoTests", true))
    }
}