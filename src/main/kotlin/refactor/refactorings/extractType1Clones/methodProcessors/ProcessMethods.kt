package refactor.refactorings.extractType1Clones.methodProcessors

import com.github.javaparser.ast.body.MethodDeclaration

class ProcessedMethod(val method: MethodDeclaration) {
    val normalisedMethod = Normaliser().normaliseMethod(method)
    val metrics = MethodMetrics().process(normalisedMethod)
}