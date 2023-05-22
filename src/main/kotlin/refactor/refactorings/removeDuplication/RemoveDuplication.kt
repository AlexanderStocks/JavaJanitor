package refactor.refactorings.removeDuplication

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.Refactoring
import refactor.refactorings.removeDuplication.common.cloneExtractors.CloneExtractor
import refactor.refactorings.removeDuplication.common.cloneExtractors.TestableCloneExtractor
import refactor.refactorings.removeDuplication.type1Clones.Type1CloneFinder
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneFinder
import refactor.refactorings.removeDuplication.type3Clones.Type3CloneFinder
import refactor.refactorings.removeDuplication.type4Clones.Type4CloneFinder
import java.nio.file.Path

class RemoveDuplication : Refactoring {
    private val threshold = 0.7

    private val cloneTypes = mapOf(
        Type1CloneFinder() to CloneExtractor(),
        Type2CloneFinder() to CloneExtractor(),
        Type3CloneFinder(threshold) to TestableCloneExtractor(),
        Type4CloneFinder(threshold) to TestableCloneExtractor()
    )

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<Path> {
        val modifiedFiles = mutableSetOf<Path>()

        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
                .filter { it.body.isPresent && it.body.get().statements.isNonEmpty }.toMutableList()
            cloneTypes.forEach { (finder, extractor) ->
                println("methods: ${methods.size}")
                val clones = finder.find(methods).filter { it.size > 1 }
                val extractedMethods = extractor.process(cu, projectRoot, clones)
                methods.removeAll(extractedMethods)

                if (extractedMethods.isNotEmpty()) {
                    modifiedFiles.add(cu.storage.get().path)
                }
            }
        }

        return modifiedFiles.toList()
    }
}
