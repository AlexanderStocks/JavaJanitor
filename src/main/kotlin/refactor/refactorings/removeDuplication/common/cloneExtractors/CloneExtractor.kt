package refactor.refactorings.removeDuplication.common.cloneExtractors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.methodCreation.MethodCreator
import tester.TestRunner
import java.nio.file.Path
import java.util.*

open class CloneExtractor {
    open fun process(
        cu: CompilationUnit,
        projectRoot: Path,
        clones: List<List<MethodDeclaration>>
    ): Pair<Int, List<MethodDeclaration>> {
        val modifiedMethods = mutableListOf<MethodDeclaration>()

        var successfulCreations = 0

        clones.forEach { cloneGroup ->
            val createdMethod = applyRefactoringAndTest(cu, cloneGroup, projectRoot)
            if (createdMethod.isPresent) {
                successfulCreations++
                modifiedMethods.addAll(cloneGroup)
                modifiedMethods.add(createdMethod.get())
            }
        }

        return Pair(successfulCreations, modifiedMethods.toList())
    }

    open fun applyRefactoringAndTest(
        cu: CompilationUnit,
        cloneGroup: List<MethodDeclaration>,
        projectRoot: Path
    ): Optional<MethodDeclaration> {
        val methodCreator = MethodCreator(cu)
        return Optional.of(methodCreator.create(cloneGroup))
    }
}