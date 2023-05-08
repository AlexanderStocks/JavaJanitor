package refactor.refactorings.removeDuplication.type1Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.cloneFinders.MetricCloneFinder
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type1CloneFinder : MetricCloneFinder() {

    override fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { it.normalisedMethod.body }.values.toList()
        }
    }

}