package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr

object Type2CloneElementReplacer {
    fun replace(method: MethodDeclaration): MethodDeclaration {
        val clonedMethod = method.clone()

        val parameterNames = clonedMethod.parameters.map { it.nameAsString }
        val variableDeclarations = clonedMethod.findAll(VariableDeclarationExpr::class.java)
        val variableDeclarators = variableDeclarations.flatMap { it.variables }

        val originalToNewNames = mutableMapOf<String, String>()

        variableDeclarators.forEachIndexed { index, variableDeclarator ->
            val originalName = variableDeclarator.nameAsString
            val newName = if (index < parameterNames.size) parameterNames[index] else "var${index + 1}"
            originalToNewNames[originalName] = newName
            variableDeclarator.setName(newName)
        }

        clonedMethod.walk { node ->
            if (node is NameExpr && node.nameAsString in originalToNewNames) {
                node.setName(originalToNewNames[node.nameAsString])
            }
        }

        return clonedMethod
    }
}

