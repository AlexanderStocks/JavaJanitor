package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration

class ProcessedMethod(val method: MethodDeclaration, transformers: List<(MethodDeclaration) -> (MethodDeclaration)> = emptyList()) {
    val normalisedMethod = MethodNormaliser().normalise(method, transformers)
    val metrics = MethodMetrics().process(normalisedMethod)
}