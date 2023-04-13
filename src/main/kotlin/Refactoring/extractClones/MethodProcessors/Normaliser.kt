package Refactoring.extractClones.MethodProcessors

import spoon.reflect.declaration.CtMethod

class Normaliser {

    private fun removeComments(method: CtMethod<*>) {
        val commentCollector = CommentCollector()
        method.accept(commentCollector)
        commentCollector.getComments().forEach { it.delete() }
    }


    fun normalizeMethod(method: CtMethod<*>): CtMethod<*> {
        val newMethod = method.copyMethod()
        removeComments(newMethod)
        return newMethod
    }


}