package refactor.refactorings.removeDuplication.type3Clones

// PDGType3CloneFinder.kt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.type3Clones.ged.GraphEditDistanceCalculator
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.SimpleEdgeCostModel
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.SimpleVertexCostModel
import refactor.refactorings.removeDuplication.type3Clones.utils.PDGBuilder

class Type3CloneFinder(private val similarityThreshold: Double) {

    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        val groupedMethods = mutableListOf<MutableList<ProcessedMethod>>()

        for (currentMethod in methodsAndMetrics) {
            val currentPdg = createPDG(currentMethod.normalisedMethod)

            var addedToGroup = false

            for (group in groupedMethods) {
                println("PDGType3CloneFinder: comparing ${group.first().normalisedMethod.nameAsString} and ${currentMethod.normalisedMethod.nameAsString}")
                val representativePdg = createPDG(group.first().normalisedMethod)

                if (areMethodsSimilar(currentPdg, representativePdg)) {
                    group.add(currentMethod)
                    addedToGroup = true
                    break
                }
            }

            if (!addedToGroup) {
                groupedMethods.add(mutableListOf(currentMethod))
            }
        }

        println("PDGType3CloneFinder: ${groupedMethods.size} groups found")
        groupedMethods.forEach { group ->
            println("PDGType3CloneFinder: ${group.size} methods in group")
        }

        return groupedMethods.filter { it.size > 1 }.map { group -> group.map { it.method } }
    }

    private fun createPDG(method: MethodDeclaration): Graph<Node, DefaultEdge> {
        val pdgBuilder = PDGBuilder(method)
        return pdgBuilder.buildPDG()
    }

    private fun areMethodsSimilar(pdg1: Graph<Node, DefaultEdge>, pdg2: Graph<Node, DefaultEdge>): Boolean {
        val edgeCost = SimpleEdgeCostModel()
        val vertexCost = SimpleVertexCostModel()

        val gedCalculator = GraphEditDistanceCalculator(pdg1, pdg2, vertexCost, edgeCost)
        val similarity = gedCalculator.computeSimilarity()
        return similarity >= similarityThreshold
    }

//    private fun groupMethodsByType3Criteria(
//        methodsWithSameMetrics: List<ProcessedMethod>,
//        averageMetricValue: Double,
//        range1Threshold: Int,
//        range2Threshold: Int
//    ): List<List<ProcessedMethod>> {
//        val groups = mutableListOf<List<ProcessedMethod>>()
//
//        for (method1 in methodsWithSameMetrics) {
//            val group = mutableListOf<ProcessedMethod>()
//
//            for (method2 in methodsWithSameMetrics) {
//                if (method1 == method2) continue
//
//                val range1 = calculateRange1(method1.metrics, averageMetricValue)
//                val range2 = calculateRange2(method1.method, method2.method)
//
//                if (range1 >= range1Threshold && range2 >= range2Threshold) {
//                    group.add(method2)
//                }
//            }
//
//            if (group.isNotEmpty()) {
//                group.add(method1)
//                groups.add(group)
//            }
//        }
//
//        return groups
//    }

    private fun calculateRange1(actualMetricValue: Double, averageMetricValue: Double): Int {
        return ((actualMetricValue * 100) / averageMetricValue).toInt()
    }

    private fun calculateRange2(method1: MethodDeclaration, method2: MethodDeclaration): Int {
        val lines1 = method1.toString().lines()
        val lines2 = method2.toString().lines()

        val minLength = minOf(lines1.size, lines2.size)
        var similarLineCount = 0

        for (i in 0 until minLength) {
            if (lines1[i].trim() == lines2[i].trim()) {
                similarLineCount++
            }
        }

        return (similarLineCount * 100) / lines1.size
    }
}
