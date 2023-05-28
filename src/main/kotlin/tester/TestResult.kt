package tester

data class TestResult(val testName: String, val testsRun: Int, val failures: Int, val errors: Int, val skipped: Int)

