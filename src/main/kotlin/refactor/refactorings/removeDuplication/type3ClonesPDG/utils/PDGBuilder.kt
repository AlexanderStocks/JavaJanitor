package refactor.refactorings.removeDuplication.type3ClonesPDG.utils

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.util.SupplierUtil
import refactor.refactorings.removeDuplication.type3ClonesPDG.graphBuilders.ControlFlowGraphBuilder
import refactor.refactorings.removeDuplication.type3ClonesPDG.graphBuilders.DataFlowGraphBuilder

class PDGBuilder(private val method: MethodDeclaration) {
    fun buildPDG(): Graph<Node, DefaultEdge> {
        val cfgBuilder = ControlFlowGraphBuilder(method)
        val cfg = cfgBuilder.buildCFG()
        
        val dfgBuilder = DataFlowGraphBuilder(method)
        val dfg = dfgBuilder.buildDFG()

        return mergeControlAndDataFlowGraphs(cfg, dfg)
    }

    private fun mergeControlAndDataFlowGraphs(cfg: Graph<Node, DefaultEdge>, dfg: Graph<Node, DefaultEdge>): Graph<Node, DefaultEdge> {
        val pdg = DefaultDirectedGraph(SupplierUtil.createSupplier(Node::class.java), SupplierUtil.createSupplier(DefaultEdge::class.java), false)

        // Add vertices to the PDG
        for (vertex in cfg.vertexSet()) {
            pdg.addVertex(vertex)
        }

        // Add control flow edges to the PDG
        for (edge in cfg.edgeSet()) {
            pdg.addEdge(cfg.getEdgeSource(edge), cfg.getEdgeTarget(edge))
        }

        // Add data flow edges to the PDG
        for (edge in dfg.edgeSet()) {
            pdg.addEdge(dfg.getEdgeSource(edge), dfg.getEdgeTarget(edge))
        }

        return pdg
    }
}
