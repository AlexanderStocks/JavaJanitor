package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.cloneFinders.MetricCloneFinder
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type2CloneFinder : MetricCloneFinder() {

    override fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { method ->
                val replaced = Type2CloneElementReplacer.replace(method.normalisedMethod)
            }.values.toList()
        }
    }
}