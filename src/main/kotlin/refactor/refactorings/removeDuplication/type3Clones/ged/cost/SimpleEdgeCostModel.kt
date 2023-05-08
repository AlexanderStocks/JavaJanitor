package refactor.refactorings.removeDuplication.type3Clones.ged.cost

import org.jgrapht.graph.DefaultEdge

class SimpleEdgeCostModel : EdgeCost {

    override fun insert(): Int = 1
    override fun delete(): Int = 1
    override fun calculate(edge1: DefaultEdge, edge2: DefaultEdge): Int = if (edge1 == edge2) 0 else 1
}