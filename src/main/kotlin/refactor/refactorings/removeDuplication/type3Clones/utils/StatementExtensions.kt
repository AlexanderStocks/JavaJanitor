package refactor.refactorings.removeDuplication.type3Clones.utils

import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.body.VariableDeclarator

fun Statement.variablesDefined(): Set<String> {
    val variableDeclarators = this.descendantsOfType<VariableDeclarator>()
    return variableDeclarators.map { it.nameAsString }.toSet()
}

fun Statement.variablesUsed(): Set<String> {
    val nameExprs = this.descendantsOfType<NameExpr>()
    return nameExprs.map { it.nameAsString }.toSet()
}

inline fun <reified T : Node> Node.descendantsOfType(): List<T> {
    val result = mutableListOf<T>()
    this.walk { node ->
        if (node is T) {
            result.add(node)
        }
    }
    return result
}
