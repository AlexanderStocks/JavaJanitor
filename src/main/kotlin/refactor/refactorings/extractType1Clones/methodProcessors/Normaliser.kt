package refactor.refactorings.extractType1Clones.methodProcessors

import com.github.javaparser.ast.body.MethodDeclaration

class Normaliser {

    private fun removeComments(method: MethodDeclaration) {
        method.allContainedComments.forEach { it.remove() }
    }

    fun normaliseMethod(method: MethodDeclaration): MethodDeclaration {
        val newMethod = method.clone()
        removeComments(newMethod)

        return newMethod
    }
}