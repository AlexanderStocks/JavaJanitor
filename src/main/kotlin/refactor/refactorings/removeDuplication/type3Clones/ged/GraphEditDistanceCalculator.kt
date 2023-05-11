package refactor.refactorings.removeDuplication.type3Clones.ged

import com.github.javaparser.ast.Node
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.EdgeCost
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.VertexCost
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.assigmentCostAlgorithms.AuctionAssignment
import refactor.refactorings.removeDuplication.type3Clones.ged.cost.assigmentCostAlgorithms.MunkresAlgorithm

class GraphEditDistanceCalculator(
    private val pdg1: Graph<Node, DefaultEdge>,
    private val pdg2: Graph<Node, DefaultEdge>,
    private val vertexCost: VertexCost,
    private val edgeCost: EdgeCost
) {
    fun computeSimilarity(): Double {
        val ged = computeGED()

        val normalizationFactor = pdg1.vertexSet().size + pdg2.vertexSet().size
        return 1 - (ged.toDouble() / normalizationFactor)
    }

    private fun computeGED(): Int {
        val pdg1Vertices = pdg1.vertexSet().toList()
        val pdg2Vertices = pdg2.vertexSet().toList()

        val n = pdg1Vertices.size
        val m = pdg2Vertices.size
        val maxSize = maxOf(n, m)

        println("GraphEditDistanceCalculator: n = $n, m = $m, maxSize = $maxSize, pdg1vertices = $pdg1Vertices, pdg2vertices $pdg2Vertices" )

        val costMatrix = Array(maxSize) { i ->
            IntArray(maxSize) { j ->
                if (i < n && j < m) {
                    vertexCost.calculate(pdg1Vertices[i], pdg2Vertices[j])
                } else {
                    maxSize * (vertexCost.delete() + vertexCost.insert())
                }
            }
        }

        println("GraphEditDistanceCalculator: costMatrix = ${costMatrix.contentDeepToString()}")

        val assignmentCost = AuctionAssignment.execute(costMatrix)
        println("GraphEditDistanceCalculator: assignmentCost = $assignmentCost")
        val edgeCosts = calculateEdgeCosts(pdg1Vertices, pdg2Vertices, costMatrix)

        return assignmentCost + edgeCosts
    }

    private fun calculateEdgeCosts(
        pdg1Vertices: List<Node>,
        pdg2Vertices: List<Node>,
        costMatrix: Array<IntArray>
    ): Int {
        println("GraphEditDistanceCalculator: calculateEdgeCosts: pdg1Vertices = $pdg1Vertices, pdg2Vertices = $pdg2Vertices, costMatrix = ${costMatrix.contentDeepToString()}")
        val edgeCosts = mutableListOf<Int>()

        for (i in pdg1Vertices.indices) {
            for (j in pdg2Vertices.indices) {
                if (costMatrix[i][j] < Int.MAX_VALUE) {
                    val pdg1Edges = pdg1.edgesOf(pdg1Vertices[i])
                    val pdg2Edges = pdg2.edgesOf(pdg2Vertices[j])

                    println("GraphEditDistanceCalculator: calculateEdgeCosts: pdg1Edges = $pdg1Edges, pdg2Edges = $pdg2Edges")


                    var cost = 0

                    for (edge1 in pdg1Edges) {
                        val source1 = pdg1.getEdgeSource(edge1)
                        val target1 = pdg1.getEdgeTarget(edge1)

                        var minCost = edgeCost.delete()

                        for (edge2 in pdg2Edges) {
                            val source2 = pdg2.getEdgeSource(edge2)
                            val target2 = pdg2.getEdgeTarget(edge2)

                            if (costMatrix[pdg1Vertices.indexOf(source1)][pdg2Vertices.indexOf(source2)] < Int.MAX_VALUE &&
                                costMatrix[pdg1Vertices.indexOf(target1)][pdg2Vertices.indexOf(target2)] < Int.MAX_VALUE
                            ) {
                                minCost = minOf(minCost, edgeCost.calculate(edge1, edge2))
                            }
                        }
                        println("GraphEditDistanceCalculator: calculateEdgeCosts: edge1 = $edge1, cost = $minCost")
                        cost += minCost
                    }

                    edgeCosts.add(cost)
                }
            }
        }
        println("GraphEditDistanceCalculator: calculateEdgeCosts: total edge cost =${edgeCosts.sum()}")

        return edgeCosts.sum()
    }
}
