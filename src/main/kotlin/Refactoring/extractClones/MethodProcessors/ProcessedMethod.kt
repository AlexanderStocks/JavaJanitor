package Refactoring.extractClones.MethodProcessors

import spoon.reflect.declaration.CtMethod

class ProcessedMethod(val method: CtMethod<*>) {
    val normalisedMethod = Normaliser().normalizeMethod(method)
    val metrics = MethodMetrics().process(normalisedMethod)
}
