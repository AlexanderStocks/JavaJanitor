package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr

object Type2CloneElementReplacer {
    fun replace(method: MethodDeclaration) {
        val variableDeclarations = method.findAll(VariableDeclarationExpr::class.java)
        val variableDeclarators = variableDeclarations.flatMap { it.variables }

        val originalToNewNames = mutableMapOf<String, String>()

        variableDeclarators.forEachIndexed { index, variableDeclarator ->
            val originalName = variableDeclarator.nameAsString
            val newName = "var${index + 1}"
            originalToNewNames[originalName] = newName
            variableDeclarator.setName(newName)
        }

        method.walk { node ->
            if (node is NameExpr && node.nameAsString in originalToNewNames) {
                node.setName(originalToNewNames[node.nameAsString])
            }
        }
    }
}