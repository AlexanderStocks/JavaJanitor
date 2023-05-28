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

    private val cloneFinders = mapOf(
        Type1CloneFinder() to CloneExtractor(),
        Type2CloneFinder() to CloneExtractor(),
        Type3CloneFinder(threshold) to TestableCloneExtractor(),
        Type4CloneFinder(threshold) to TestableCloneExtractor()
    )

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        val modifiedFiles = mutableSetOf<CompilationUnit>()

        var cuCounter = 0

        var totalClones = 0
        var totalType1ClonesFound = 0
        var totalType1ClonesCreated = 0
        var totalType2ClonesFound = 0
        var totalType2ClonesCreated = 0
        var totalType3ClonesFound = 0
        var totalType3ClonesCreated = 0
        var totalType4ClonesFound = 0
        var totalType4ClonesCreated = 0

        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
                .filter { it.body.isPresent && it.body.get().statements.isNonEmpty }.toMutableList()

            cuCounter++
            println("CU $cuCounter/${cus.size}")

            var cloneFinderCounter = 0
            cloneFinders.forEach { (finder, extractor) ->
                val clones = finder.find(methods).filter { it.size > 1 }
                if (clones.isNotEmpty()) {
                    totalClones += clones.size
                    when (finder) {
                        is Type4CloneFinder -> {
                            totalType4ClonesFound += clones.size
                        }

                        is Type3CloneFinder -> {
                            totalType3ClonesFound += clones.size
                        }

                        is Type2CloneFinder -> {
                            totalType2ClonesFound += clones.size
                        }

                        is Type1CloneFinder -> {
                            totalType1ClonesFound += clones.size
                        }
                    }

                    val (sucessfulCreations, extractedMethods) = extractor.process(cu, projectRoot, clones)
                    when (finder) {
                        is Type4CloneFinder -> {
                            totalType4ClonesCreated += sucessfulCreations
                        }

                        is Type3CloneFinder -> {
                            totalType3ClonesCreated += sucessfulCreations
                        }

                        is Type2CloneFinder -> {
                            totalType2ClonesCreated += sucessfulCreations
                        }

                        is Type1CloneFinder -> {
                            totalType1ClonesCreated += sucessfulCreations
                        }
                    }
                    methods.removeAll(extractedMethods)

                    if (extractedMethods.isNotEmpty()) {
                        modifiedFiles.add(cu)
                    }
                }

                cloneFinderCounter++
                println("Clone finder $cloneFinderCounter/${cloneFinders.size}")
            }
        }
        println("Total clones found: $totalClones")
        println("Total Type1 clones found: $totalType1ClonesFound, created: $totalType1ClonesCreated")
        println("Total Type2 clones found: $totalType2ClonesFound, created: $totalType2ClonesCreated")
        println("Total Type3 clones found: $totalType3ClonesFound, created: $totalType3ClonesCreated")
        println("Total Type4 clones found: $totalType4ClonesFound, created: $totalType4ClonesCreated")
        return modifiedFiles.toList()
    }

}
