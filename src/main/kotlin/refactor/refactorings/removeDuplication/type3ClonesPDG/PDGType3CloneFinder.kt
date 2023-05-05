package refactor.refactorings.removeDuplication.type3ClonesPDG

// PDGType3CloneFinder.kt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.type3ClonesPDG.ged.GraphEditDistanceCalculator
import refactor.refactorings.removeDuplication.type3ClonesPDG.ged.cost.SimpleEdgeCostModel
import refactor.refactorings.removeDuplication.type3ClonesPDG.ged.cost.SimpleVertexCostModel
import refactor.refactorings.removeDuplication.type3ClonesPDG.utils.PDGBuilder

class PDGType3CloneFinder(private val similarityThreshold: Double) {

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

        return groupedMethods.map { group -> group.map { it.method } }
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
}
