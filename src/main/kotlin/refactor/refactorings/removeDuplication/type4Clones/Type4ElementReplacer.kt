package refactor.refactorings.removeDuplication.type4Clones

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.ReferenceType
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer
import kotlin.jvm.optionals.getOrNull

object Type4CloneElementReplacer {
    fun replace(method: MethodDeclaration): MethodDeclaration {
        method.walk { node ->
            when (node) {
                is ForStmt, is DoStmt -> {
                    // Convert for loop to while loop
                    val stmt = toWhileStmt(node)
                    node.parentNode.get().replace(stmt)
                }

                is IfStmt, is SwitchStmt, is ConditionalExpr -> {
                    // Convert to if-else statement
                    val ifStmt = toIfStmt(node)
                    println("ifStmt: $ifStmt")
                    println("node: ${node.parentNode.getOrNull()}")
                    node.replace(ifStmt)
                }
            }
        }

        println("method: $method")

        return Type2CloneElementReplacer.replace(method)
    }

    private fun toWhileStmt(node: Node): Node {
        when (node) {
            is ForStmt -> {
                // For initialization, we assume there's only one initialization and it's an ExpressionStmt.
                val initialization = node.initialization.first.get().clone()

                // For the condition part, we directly use the comparison part of ForStmt.
                val condition = node.compare.get().clone()

                // For the update part, we assume there's only one update and it's an ExpressionStmt.
                // We append it at the end of the body of ForStmt.
                val body = BlockStmt()

                node.body.asBlockStmt().statements.forEach { statement ->
                    body.addStatement(statement.clone())
                }

                node.update.forEach { update ->
                    body.addStatement(ExpressionStmt(update.clone()))
                }

                // We put the initialization part before the new WhileStmt.
                val whileStmt = WhileStmt(condition, body)


                // If initialization is a declaration, we create a block statement to scope it properly.
                return if (initialization is VariableDeclarationExpr) {
                    val blockStmt = BlockStmt()
                    blockStmt.addStatement(initialization)
                    blockStmt.addStatement(whileStmt)
                    blockStmt
                } else {
                    // If initialization is not a declaration, it doesn't need scoping and we can just use the while statement.
                    whileStmt
                }
            }

            is DoStmt -> {
                // DoStmt is similar to WhileStmt but only differs in the order of condition checking and statement execution.
                // So we can directly use the same condition and body for the new WhileStmt.
                val condition = node.condition.clone()
                val firstIteration = node.body.clone()


                val body = node.body.clone()
                val blockStmt = BlockStmt()
                firstIteration.asBlockStmt().statements.forEach { stmt ->
                    blockStmt.addStatement(stmt)
                }

                blockStmt.addStatement(WhileStmt(condition, body))
                return blockStmt
            }

            else -> throw IllegalArgumentException("Unsupported node type for toWhileStmt: ${node.javaClass.name}")
        }
    }


    private fun toIfStmt(node: Node): Node {
        return when (node) {
            is IfStmt -> {
                // If it's already an IfStmt, just clone it.
                return node.clone()
            }

            is ConditionalExpr -> {
                // If the ternary operator is used as an expression, replace it with a dummy literal
                val dummyLiteral = NullLiteralExpr() // Or some other unique literal
                node.replace(dummyLiteral)

                // Find the parent node where the ternary operator is used
                val parentNode = dummyLiteral.findAncestor(Statement::class.java)
                    ?: throw IllegalArgumentException("Unsupported usage of ternary operator.")
                val (varName, nullType) = getVariableInfo(parentNode.get().asExpressionStmt())

                val ifStmt = IfStmt(
                    node.condition.clone(),
                    BlockStmt().addStatement(
                        AssignExpr(
                            NameExpr(varName),
                            node.thenExpr.clone(),
                            AssignExpr.Operator.ASSIGN
                        )
                    ),
                    BlockStmt().addStatement(
                        AssignExpr(
                            NameExpr(varName),
                            node.elseExpr.clone(),
                            AssignExpr.Operator.ASSIGN
                        )
                    )
                )

                // Replace the dummy literal with the temporary variable
                parentNode.get().walk { n ->
                    if (n == dummyLiteral) {
                        n.replace(NameExpr(nullType))
                    }
                }

                // Replace the original statement with the new block statement

                parentNode.get().parentNode.get()
                    .replace(BlockStmt().addStatement(parentNode.get().clone()).addStatement(ifStmt))

                parentNode.get().clone()
            }


            is SwitchStmt -> {
                // For SwitchStmt, convert it into nested IfStmts.
                val entries = node.entries

                if (entries.isEmpty()) {
                    throw IllegalArgumentException("Empty SwitchStmt cannot be converted to IfStmt.")
                }

                var ifStmt: IfStmt? = null
                var defaultStmt: Statement? = null
                for (i in entries.indices.reversed()) {
                    val entry = entries[i]
                    if (entry.labels.isNonEmpty) {
                        val condition =
                            BinaryExpr(node.selector.clone(), entry.labels.first.get(), BinaryExpr.Operator.EQUALS)

                        val stmt =
                            BlockStmt().also { block -> entry.statements.filter { it !is BreakStmt }.forEach { block.addStatement(it.clone()) } }
                        ifStmt = if (ifStmt == null) {
                            IfStmt(condition, stmt, defaultStmt)
                        } else {
                            IfStmt(condition, stmt, ifStmt)
                        }
                    } else {
                        defaultStmt =
                            BlockStmt().also { block -> entry.statements.filter { it !is BreakStmt }.forEach { block.addStatement(it.clone()) } }
                    }
                }
                ifStmt ?: throw IllegalArgumentException("Failed to convert SwitchStmt to IfStmt.")
            }

            else -> throw IllegalArgumentException("Unsupported node type for toIfStmt: ${node.javaClass.name}")
        }
    }

    private fun getVariableInfo(stmt: ExpressionStmt): Pair<String, String> {
        val expr = stmt.expression
        if (expr is VariableDeclarationExpr) {
            val varName = expr.variables[0].nameAsString
            val varType = expr.commonType
            val nullType = when (expr.commonType) {
                is PrimitiveType -> {
                    when (varType) {
                        PrimitiveType.booleanType() -> "false"
                        PrimitiveType.charType() -> "''"
                        PrimitiveType.byteType() -> "0.toByte()"
                        PrimitiveType.shortType() -> "0.toShort()"
                        PrimitiveType.intType() -> "0"
                        PrimitiveType.longType() -> "0L"
                        PrimitiveType.floatType() -> "0.0f"
                        PrimitiveType.doubleType() -> "0.0"
                        else -> "null"
                    }
                }

                is ReferenceType -> "null"
                else -> throw IllegalArgumentException("Unsupported type")
            }
            return Pair(varName, nullType)
        } else {
            throw IllegalArgumentException("ExpressionStmt does not contain a VariableDeclarationExpr")
        }
    }
}
