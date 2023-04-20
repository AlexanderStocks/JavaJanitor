package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration

class ProcessedMethod(val method: MethodDeclaration) {
    val normalisedMethod = MethodNormaliser().normalise(method)
    val metrics = MethodMetrics().process(normalisedMethod)
}