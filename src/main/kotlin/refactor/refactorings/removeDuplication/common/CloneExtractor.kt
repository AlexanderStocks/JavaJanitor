package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.Refactoring
import refactor.refactorings.removeDuplication.common.cloneFinders.CloneFinder
import tester.TestRunner
import java.nio.file.Path

abstract class CloneExtractor : Refactoring {
    protected abstract val cloneFinder: CloneFinder
    protected open val elementReplacers: List<(MethodDeclaration) -> MethodDeclaration> = emptyList()
    protected open val requiresTesting: Boolean = false

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<Path> {
        val modifiedFiles = mutableSetOf<Path>()

        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
            val processedMethods = methods.map { ProcessedMethod(it, elementReplacers) }
            val clones = cloneFinder.find(processedMethods)

            if (clones.isNotEmpty()) {
                val originalCu = cu.clone()
                val success : Boolean = if (requiresTesting) applyRefactoringAndTest(cu, clones, projectRoot) else true

                if (success) {
                    modifiedFiles.add(cu.storage.get().path)
                } else {
                    cu.replace(originalCu)
                }
            }
        }

        return modifiedFiles.toList()
    }

    private fun applyRefactoringAndTest(
        cu: CompilationUnit,
        clones: List<List<MethodDeclaration>>,
        projectRoot: Path
    ): Boolean {
        val testRunner = TestRunner.create(projectRoot.toString())
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
