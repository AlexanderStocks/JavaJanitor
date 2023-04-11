package Refactoring.extractClones

import spoon.reflect.declaration.CtMethod

class Normaliser {
    fun normalizeMethod(method: CtMethod<*>): CtMethod<*> {
        val newMethod = method.copyMethod()

        val commentCollector = CommentCollector()
        newMethod.accept(commentCollector)
        commentCollector.getComments().forEach { it.delete() }
        return newMethod
    }
}