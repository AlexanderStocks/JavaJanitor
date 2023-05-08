package refactor.refactorings.removeDuplication.type3Clones.ged.cost

import com.github.javaparser.ast.Node

class SimpleVertexCostModel : VertexCost {
    override fun insert(): Int = 1
    override fun delete(): Int = 1
    override fun calculate(vertex1: Node, vertex2: Node): Int = if (vertex1 == vertex2) 0 else 1
}