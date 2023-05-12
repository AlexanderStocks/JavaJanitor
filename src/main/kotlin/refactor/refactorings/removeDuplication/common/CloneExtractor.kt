package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.Refactoring
import tester.TestRunner
import java.nio.file.Path

abstract class CloneExtractor : Refactoring {

    protected abstract fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>>

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<Path> {
        val modifiedFiles = mutableSetOf<Path>()

        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
            val processedMethods = methods.map { ProcessedMethod(it) }
            val clones = findClones(processedMethods)

            if (clones.isNotEmpty()) {
                MethodCreator(cu, clones).create()
                modifiedFiles.add(cu.storage.get().path)
            }
        }
        return modifiedFiles.toList()
    }

    protected fun applyRefactoringAndTest(
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