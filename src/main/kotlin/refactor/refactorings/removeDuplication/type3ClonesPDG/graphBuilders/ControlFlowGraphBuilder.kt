package refactor.refactorings.removeDuplication.type3ClonesPDG.graphBuilders

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.*
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.util.SupplierUtil

class ControlFlowGraphBuilder(private val method: MethodDeclaration) {
    fun buildCFG(): Graph<Node, DefaultEdge> {
        val cfg = DefaultDirectedGraph(SupplierUtil.createSupplier(Node::class.java), SupplierUtil.createSupplier(DefaultEdge::class.java), false)

        val stmts = method.body.get().statements
        createNodes(cfg, stmts)
        connectNodes(cfg, stmts)

        return cfg
    }

    private fun createNodes(cfg: Graph<Node, DefaultEdge>, stmts: NodeList<Statement>) {
        for (stmt in stmts) {
            cfg.addVertex(stmt)
            if (stmt is IfStmt || stmt is WhileStmt || stmt is ForStmt || stmt is TryStmt) {
                val containedStmts = getContainedStatements(stmt)
                createNodes(cfg, containedStmts)
            }
        }
    }

    private fun connectNodes(cfg: Graph<Node, DefaultEdge>, stmts: NodeList<Statement>) {
        for (i in 0 until stmts.size - 1) {
            val currentStmt = stmts[i]
            val nextStmt = stmts[i + 1]

            if (currentStmt is IfStmt) {
                val thenStmts = NodeList(currentStmt.thenStmt)
                val elseStmts = if (currentStmt.elseStmt.isPresent) NodeList(currentStmt.elseStmt.get()) else NodeList()

                connectNodes(cfg, thenStmts)
                connectNodes(cfg, elseStmts)

                cfg.addEdge(currentStmt, thenStmts.first())
                if (elseStmts.isNotEmpty()) {
                    cfg.addEdge(currentStmt, elseStmts.first())
                }
                cfg.addEdge(thenStmts.last(), nextStmt)
                if (elseStmts.isNotEmpty()) {
                    cfg.addEdge(elseStmts.last(), nextStmt)
                }
            } else if (currentStmt is WhileStmt || currentStmt is ForStmt) {
                val bodyStmts = getContainedStatements(currentStmt)

                connectNodes(cfg, bodyStmts)

                cfg.addEdge(currentStmt, bodyStmts.first())
                cfg.addEdge(bodyStmts.last(), nextStmt)
            } else if (currentStmt is TryStmt) {
                val tryStmts = NodeList<Statement>(currentStmt.tryBlock)
                val finallyStmts = if (currentStmt.finallyBlock.isPresent) NodeList<Statement>(currentStmt.finallyBlock.get()) else NodeList<Statement>()

                connectNodes(cfg, tryStmts)
                connectNodes(cfg, finallyStmts)

                cfg.addEdge(currentStmt, tryStmts.first())
                cfg.addEdge(tryStmts.last(), nextStmt)
                if (finallyStmts.isNotEmpty()) {
                    cfg.addEdge(tryStmts.last(), finallyStmts.first())
                    cfg.addEdge(finallyStmts.last(), nextStmt)
                }
            } else {
                cfg.addEdge(currentStmt, nextStmt)
            }
        }
    }

    private fun getContainedStatements(stmt: Statement): NodeList<Statement> {
        return when (stmt) {
            is IfStmt -> {
                NodeList(stmt.thenStmt).also { if (stmt.elseStmt.isPresent) it.add(stmt.elseStmt.get()) }
            }
            is WhileStmt -> {
                if (stmt.body is BlockStmt) {
                    (stmt.body as BlockStmt).statements
                } else {
                    NodeList<Statement>(stmt.body)
                }
            }
            is ForStmt -> {
                if (stmt.body is BlockStmt) {
                    (stmt.body as BlockStmt).statements
                } else {
                    NodeList<Statement>(stmt.body)
                }
            }
            is TryStmt -> {
                NodeList<Statement>(stmt.tryBlock).also { if (stmt.finallyBlock.isPresent) it.add(stmt.finallyBlock.get()) }
            }
            else -> NodeList<Statement>()
        }
    }
}
