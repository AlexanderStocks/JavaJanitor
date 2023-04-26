package refactor.refactorings.removeDuplication.type3ClonesOld

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer

object Type3CloneElementReplacer {
    fun replace(method: MethodDeclaration): MethodDeclaration {
        val clonedMethod = Type2CloneElementReplacer.replace(method)

        clonedMethod.walk(Expression::class.java) { expression ->
            if (expression is LiteralExpr) {
                expression.replaceWithPlaceholder()
            }
        }

        return clonedMethod
    }

    private fun LiteralExpr.replaceWithPlaceholder() {
        val placeholder: Expression = when (this) {
            is BooleanLiteralExpr -> NameExpr("BOOLEAN_PLACEHOLDER")
            is CharLiteralExpr -> NameExpr("CHAR_PLACEHOLDER")
            is DoubleLiteralExpr -> NameExpr("DOUBLE_PLACEHOLDER")
            is IntegerLiteralExpr -> NameExpr("INTEGER_PLACEHOLDER")
            is LongLiteralExpr -> NameExpr("LONG_PLACEHOLDER")
            is StringLiteralExpr -> NameExpr("STRING_PLACEHOLDER")
            else -> this // Do nothing for other literal types
        }
        this.replace(placeholder)
    }
}
