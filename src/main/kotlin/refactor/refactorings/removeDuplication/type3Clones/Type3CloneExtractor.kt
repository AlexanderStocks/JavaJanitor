package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.MethodCreator
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import tester.TestRunner
import java.nio.file.Path

class Type3CloneExtractor : CloneExtractor() {
    private val similarityThreshold = 0.8
    private val type3CloneFinder = Type3CloneFinder(similarityThreshold)

    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return type3CloneFinder.find(processedMethods)
    }

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<Path> {
        val testRunner = TestRunner.create(projectRoot.toString())

        val modifiedFiles = mutableSetOf<Path>()

        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
            val processedMethods = methods.map { ProcessedMethod(it) }
            val clones = findClones(processedMethods)

            if (clones.isNotEmpty()) {
                val originalCu = cu.clone()
                val success = applyRefactoringAndTest(cu, clones, testRunner)

                if (success) {
                    modifiedFiles.add(cu.storage.get().path)
                } else {
                    cu.replace(originalCu)
                }
            }
        }

        println("Modified files: $modifiedFiles")

        return modifiedFiles.toList()
    }

    private fun applyRefactoringAndTest(
        cu: CompilationUnit,
        clones: List<List<MethodDeclaration>>,
        testRunner: TestRunner
    ): Boolean {
        clones.forEach { cloneGroup ->
            val originalCu = cu.clone()
            MethodCreator(cu, listOf(cloneGroup)).create()
            val testResults = testRunner.runTests()

            if (testResults.all { it.isSuccessful }) {
                return true
            } else {
                cu.replace(originalCu)
            }
        }

        return false
    }
}
