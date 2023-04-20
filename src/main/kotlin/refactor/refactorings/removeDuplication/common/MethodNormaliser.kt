package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration

class MethodNormaliser {

    private fun removeComments(method: MethodDeclaration) {
        method.allContainedComments.forEach { it.remove() }
    }

    fun normalise(method: MethodDeclaration): MethodDeclaration {
        val newMethod = method.clone()
        removeComments(newMethod)

        return newMethod
    }
}