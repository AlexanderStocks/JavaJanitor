package refactor.refactorings.recursionToIteration

import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.UnaryExpr

class ConditionInverter {
    fun inverseCondition(condition: Expression): Expression {
        // Logical complement for simple conditions
        if (condition is UnaryExpr && condition.operator == UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
            return condition.expression.clone()
        }

        if (condition is BooleanLiteralExpr) {
            return BooleanLiteralExpr(!condition.value)
        }

        // Inverting relational operators
        if (condition is BinaryExpr) {
            val operator = condition.operator
            val left = condition.left.clone()
            val right = condition.right.clone()

            return when (operator) {
                BinaryExpr.Operator.EQUALS -> BinaryExpr(left, right, BinaryExpr.Operator.NOT_EQUALS)
                BinaryExpr.Operator.NOT_EQUALS -> BinaryExpr(left, right, BinaryExpr.Operator.EQUALS)
                BinaryExpr.Operator.LESS -> BinaryExpr(left, right, BinaryExpr.Operator.GREATER)
                BinaryExpr.Operator.GREATER -> BinaryExpr(left, right, BinaryExpr.Operator.LESS)
                BinaryExpr.Operator.LESS_EQUALS -> BinaryExpr(left, right, BinaryExpr.Operator.GREATER_EQUALS)
                BinaryExpr.Operator.GREATER_EQUALS -> BinaryExpr(left, right, BinaryExpr.Operator.LESS_EQUALS)
                BinaryExpr.Operator.AND -> BinaryExpr(inverseCondition(left), inverseCondition(right), BinaryExpr.Operator.OR)
                BinaryExpr.Operator.OR -> BinaryExpr(inverseCondition(left), inverseCondition(right), BinaryExpr.Operator.AND)
                else -> throw UnsupportedOperationException("Unsupported binary operator for condition inversion: $operator")
            }
        }

        throw IllegalArgumentException("Unsupported condition type for inversion: ${condition.javaClass.name}")
    }
}