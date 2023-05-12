package refactor.refactorings.removeDuplication.type3Clones

// PDGType3CloneFinder.kt

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.common.cloneFinders.ThresholdCloneFinder
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer
import refactor.refactorings.removeDuplication.type3Clones.ged.GraphEditDistanceCalculator
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.SimpleEdgeCostModel
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.SimpleVertexCostModel
import refactor.refactorings.removeDuplication.type3Clones.utils.PDGBuilder

open class Type3CloneFinder(private val similarityThreshold: Double) : ThresholdCloneFinder(similarityThreshold) {

    override fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics, ::groupMethodsByRange).flatMap { potentialClones ->
            val pdgCache = mutableMapOf<MethodDeclaration, Graph<Node, DefaultEdge>>()
            val groupedMethods = mutableListOf<MutableList<ProcessedMethod>>()

            for (currentMethod in potentialClones) {
                val currentPdg =
                    pdgCache.getOrPut(currentMethod.normalisedMethod) { createPDG(Type2CloneElementReplacer.replace(currentMethod.normalisedMethod)) }

                var addedToGroup = false

                for (group in groupedMethods) {
                    val representativeMethod = group.first().normalisedMethod
                    val representativePdg = pdgCache.getOrPut(representativeMethod) { createPDG(Type2CloneElementReplacer.replace(representativeMethod)) }

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

            groupedMethods.filter { it.size > 1 }.map { group -> group.map { it.method } }
        }
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
