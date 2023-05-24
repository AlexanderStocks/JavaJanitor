package refactor.refactorings.removeDuplication.common.cloneExtractors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.methodCreation.MethodCreator
import java.nio.file.Path
import java.util.*

open class CloneExtractor {
    open fun process(
        cu: CompilationUnit,
        projectRoot: Path,
        clones: List<List<MethodDeclaration>>
    ): List<MethodDeclaration> {
        val modifiedMethods = mutableListOf<MethodDeclaration>()

        clones.forEach { cloneGroup ->
            val originalCu = cu.clone()
            val createdMethod = applyRefactoringAndTest(cu, cloneGroup, projectRoot)
            if (createdMethod.isPresent) {
                modifiedMethods.addAll(cloneGroup)
                modifiedMethods.add(createdMethod.get())
            } else {
                cu.replace(originalCu)
            }
        }

        return modifiedMethods.toList()
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