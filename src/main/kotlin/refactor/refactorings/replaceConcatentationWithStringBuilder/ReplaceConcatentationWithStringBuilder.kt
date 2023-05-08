package refactor.refactorings.replaceConcatentationWithStringBuilder

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.StringLiteralExpr
import refactor.Refactoring
import java.nio.file.Path

class ReplaceConcatenationWithStringBuilder : Refactoring {
    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<Path> {
        return cus.mapNotNull { replaceConcatenationWithStringBuilderAndSave(it) }
    }

    private fun replaceConcatenationWithStringBuilderAndSave(cu: CompilationUnit): Path? {
        val originalCuString = cu.toString()
        replaceConcatenationWithStringBuilder(cu)

        return if (originalCuString != cu.toString()) cu.storage.get().path else null
    }

    private fun replaceConcatenationWithStringBuilder(cu: CompilationUnit) {
        val binaryExprs = cu.findAll(BinaryExpr::class.java)

        binaryExprs.filter { it.operator == BinaryExpr.Operator.PLUS }
            .filter { it.left is StringLiteralExpr || it.right is StringLiteralExpr }
            .forEach { binaryExpr ->
                val stringBuilderExpr = createStringBuilderExpression(binaryExpr)
                binaryExpr.replace(stringBuilderExpr)
            }
    }

    private fun createStringBuilderExpression(binaryExpr: BinaryExpr): Expression {
        val stringBuilder = StringBuilder("new StringBuilder()")

        fun appendExpressionsRecursively(expr: Expression) {
            if (expr is BinaryExpr && expr.operator == BinaryExpr.Operator.PLUS) {
                appendExpressionsRecursively(expr.left)
                stringBuilder.append(".append(${expr.right})")
            } else {
                stringBuilder.append(".append(${expr})")
            }
        }

        appendExpressionsRecursively(binaryExpr)

        return StaticJavaParser.parseExpression(stringBuilder.toString())
    }

}
