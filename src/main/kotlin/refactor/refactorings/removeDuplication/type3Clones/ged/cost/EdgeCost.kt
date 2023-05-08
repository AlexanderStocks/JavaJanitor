package refactor.refactorings.removeDuplication.type3Clones.ged.cost

import org.jgrapht.graph.DefaultEdge

interface EdgeCost {
    fun insert(): Int
    fun delete(): Int
    fun calculate(edge1: DefaultEdge, edge2: DefaultEdge): Int
}