package refactor.refactorings.collapseNestedIfStatements

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.IfStmt
import refactor.Refactoring
import java.nio.file.Path

class CollapseNestedIfStatements : Refactoring {
    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        return cus.mapNotNull { collapseNestedIfStatementsAndSave(it) }
    }

    private fun collapseNestedIfStatementsAndSave(cu: CompilationUnit): CompilationUnit? {
        val originalCuString = cu.toString()
        collapseNestedIfStatements(cu)

        return if (originalCuString != cu.toString()) cu else null
    }

    private fun collapseNestedIfStatements(cu: CompilationUnit) {
        val ifStmts = cu.findAll(IfStmt::class.java)

        ifStmts.forEach { ifStmt ->
            if (ifStmt.elseStmt.isPresent) return@forEach

            if (ifStmt.thenStmt is BlockStmt) {
                val blockStmt = ifStmt.thenStmt as BlockStmt

                // Only perform the refactoring if the block contains a single statement
                // that is an IfStmt and does not have an else statement.
                if (blockStmt.statements.size == 1
                    && blockStmt.statements[0] is IfStmt
                    && !(blockStmt.statements[0] as IfStmt).elseStmt.isPresent) {
                    val nestedIfStmt = blockStmt.statements[0] as IfStmt
                    val combinedCondition = BinaryExpr(ifStmt.condition, nestedIfStmt.condition, BinaryExpr.Operator.AND)
                    ifStmt.condition = combinedCondition
                    ifStmt.thenStmt = nestedIfStmt.thenStmt
                }
            }
        }
    }

}
