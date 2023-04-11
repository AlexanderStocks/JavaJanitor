package Refactoring.extractClones

import spoon.reflect.declaration.CtMethod

class NormalisedMethodWithMetrics(val method: CtMethod<*>) {
    val normalisedMethod = Normaliser().normalizeMethod(method)
    val metrics = MethodMetrics().process(normalisedMethod)
}
