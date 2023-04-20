package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.Refactoring
import java.nio.file.Path

abstract class CloneExtractor : Refactoring {

    protected abstract fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>>

    override fun process(cus: List<CompilationUnit>): List<Path> {
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
}