package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod

interface CloneFinder {
    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>>
}
