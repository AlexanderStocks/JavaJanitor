package refactor.refactorings.removeDuplication.type3Clones.graphBuilders

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.Statement
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.util.SupplierUtil
import refactor.refactorings.removeDuplication.type3Clones.utils.variablesDefined
import refactor.refactorings.removeDuplication.type3Clones.utils.variablesUsed

class DataFlowGraphBuilder(private val method: MethodDeclaration) {
    fun buildDFG(): Graph<Node, DefaultEdge> {
        val cfgBuilder = ControlFlowGraphBuilder(method)
        val cfg = cfgBuilder.buildCFG()

        val dfg = DefaultDirectedGraph(SupplierUtil.createSupplier(Node::class.java), SupplierUtil.createSupplier(DefaultEdge::class.java), false)

        createNodes(dfg, cfg)
        connectNodes(dfg, cfg)

        return dfg
    }

    private fun createNodes(dfg: Graph<Node, DefaultEdge>, cfg: Graph<Node, DefaultEdge>) {
        for (vertex in cfg.vertexSet()) {
            dfg.addVertex(vertex)
        }
    }

    private fun connectNodes(dfg: Graph<Node, DefaultEdge>, cfg: Graph<Node, DefaultEdge>) {
        for (vertex in cfg.vertexSet()) {
            val outEdges = cfg.outgoingEdgesOf(vertex)
            for (outEdge in outEdges) {
                val target = cfg.getEdgeTarget(outEdge)
                val sourceStmt = vertex as Statement
                val targetStmt = target as Statement

                if (hasDataDependency(sourceStmt, targetStmt)) {
                    dfg.addEdge(vertex, target)
                }
            }
        }
    }

    private fun hasDataDependency(sourceStmt: Statement, targetStmt: Statement): Boolean {
        // Analyze data dependencies between sourceStmt and targetStmt
        // For simplicity, focus on local variable dependencies

        val sourceVariables = sourceStmt.variablesDefined()
        val targetVariables = targetStmt.variablesUsed()

        return sourceVariables.any { it in targetVariables }
    }
}
