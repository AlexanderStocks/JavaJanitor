package refactor.refactorings.removeDuplication.type3ClonesPDG.ged.cost

import com.github.javaparser.ast.Node

interface VertexCost {
    fun insert(): Int
    fun delete(): Int
    fun calculate(vertex1: Node, vertex2: Node): Int
}