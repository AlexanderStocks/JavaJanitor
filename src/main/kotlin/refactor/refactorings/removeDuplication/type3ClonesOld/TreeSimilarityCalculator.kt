package refactor.refactorings.removeDuplication.type3ClonesOld

import com.github.javaparser.ast.Node

class TreeSimilarityCalculator {

    fun calculateSimilarity(node1: Node, node2: Node): Double {
        val commonNodeCount = countCommonNodes(node1, node2)
        val totalNodeCount = countNodes(node1) + countNodes(node2)

        return 2.0 * commonNodeCount / totalNodeCount.toDouble()
    }

    private fun countCommonNodes(node1: Node, node2: Node): Int {
        if (node1.javaClass != node2.javaClass) return 0

        val children1 = node1.childNodes
        val children2 = node2.childNodes

        val commonNodeCount = children1.zip(children2)
            .sumOf { (child1, child2) -> countCommonNodes(child1, child2) }

        return if (node1.toString() == node2.toString()) commonNodeCount + 1 else commonNodeCount
    }

    private fun countNodes(node: Node): Int {
        val childNodeCount = node.childNodes.sumOf { countNodes(it) }
        return childNodeCount + 1
    }
}
