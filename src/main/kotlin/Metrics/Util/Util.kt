package Metrics.Util

import spoon.reflect.declaration.CtType

object Util {
    fun isValid(element: CtType<*>?): Boolean {
        return element?.qualifiedName != null && (element.isClass || element.isInterface)
                && !element.isAnonymous && !element.isLocalType && !element.isShadow
    }
}