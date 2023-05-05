package refactor.refactorings.removeDuplication.type3ClonesOld

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.MetricCloneFinder
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type3CloneFinder(private val similarityThreshold: Double) : MetricCloneFinder() {
    private val treeSimilarityCalculator = TreeSimilarityCalculator()

    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            val groupedMethods = mutableListOf<MutableList<ProcessedMethod>>()

            methodsWithSameMetrics.forEach { currentMethod ->
                val currentMethodWithElementsReplaced =
                    Type3CloneElementReplacer.replace(currentMethod.normalisedMethod)


                var addedToGroup = false

                for (group in groupedMethods) {
                    val representativeMethodWithElementsReplaced =
                        Type3CloneElementReplacer.replace(group.first().normalisedMethod)

                    if (areMethodsSimilar(
                            currentMethodWithElementsReplaced,
                            representativeMethodWithElementsReplaced
                        )
                    ) {
                        group.add(currentMethod)
                        addedToGroup = true
                        break
                    }
                }

                if (!addedToGroup) {
                    groupedMethods.add(mutableListOf(currentMethod))
                }
            }

            groupedMethods
        }
    }

    private fun areMethodsSimilar(
        method1: MethodDeclaration,
        method2: MethodDeclaration
    ): Boolean {
        val similarity = treeSimilarityCalculator.calculateSimilarity(
            method1,
            method2
        )
        return similarity >= similarityThreshold
    }
}
