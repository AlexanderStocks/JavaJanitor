package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration

class MethodNormaliser {

    private fun removeComments(method: MethodDeclaration) {
        method.allContainedComments.forEach { it.remove() }
    }

    fun normalise(method: MethodDeclaration, transformers: List<(MethodDeclaration) -> MethodDeclaration>): MethodDeclaration {
        var newMethod = method.clone()
        removeComments(newMethod)

        transformers.forEach { transformer ->
            newMethod = transformer(newMethod)
        }

        return newMethod
    }
}