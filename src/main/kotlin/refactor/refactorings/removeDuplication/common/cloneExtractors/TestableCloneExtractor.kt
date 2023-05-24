package refactor.refactorings.removeDuplication.common.cloneExtractors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import tester.TestRunner
import java.nio.file.Path
import java.util.*

class TestableCloneExtractor : CloneExtractor() {
    override fun applyRefactoringAndTest(
        cu: CompilationUnit,
        cloneGroup: List<MethodDeclaration>,
        projectRoot: Path
    ): Optional<MethodDeclaration> {
        return try {
            val createdMethod = super.applyRefactoringAndTest(cu, cloneGroup, projectRoot)
            val testRunner = TestRunner.create(projectRoot.toString())

            if (createdMethod.isPresent) {
                val testResults = testRunner.runTests()
                if (!testResults.all { it.isSuccessful }) {
                    return Optional.empty()
                }
            }

            createdMethod
        } catch (e: Exception) {
            println("Error during refactoring and testing: ${e.message}")
            e.printStackTrace()
            Optional.empty()
        }
    }

}