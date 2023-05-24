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

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        val modifiedFiles = mutableSetOf<CompilationUnit>()

        cus.forEach { cu ->
            println("Processing Compilation Unit: ${cu.storage.get().path}") // Debugging

            val methods = cu.findAll(MethodDeclaration::class.java)
                .filter { it.body.isPresent && it.body.get().statements.isNonEmpty }.toMutableList()

            cloneTypes.forEach { (finder, extractor) ->
                val clones = finder.find(methods).filter { it.size > 1 }

                println("Found ${clones.size} clone(s) with finder: ${finder.javaClass.simpleName}") // Debugging

                val extractedMethods = extractor.process(cu, projectRoot, clones)
                methods.removeAll(extractedMethods)

                if (extractedMethods.isNotEmpty()) {
                    println("Extracted methods with extractor: ${extractor.javaClass.simpleName}") // Debugging
                    modifiedFiles.add(cu)
                }
            }
        }

        println("Modified files: ${modifiedFiles.joinToString()}") // Debugging

        return modifiedFiles.toList()
    }

}
