package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod

abstract class BaseCloneFinder : CloneFinder {
    protected fun findClones(
        methods: List<ProcessedMethod>,
        groupByCloneCriteria: (List<ProcessedMethod>) -> List<List<ProcessedMethod>>
    ): List<List<ProcessedMethod>> {
        val groupedByMetrics = methods.groupBy { it.metrics }
        val groupsWithSameNormalizedBody = mutableListOf<List<ProcessedMethod>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByCloneCriteria = groupByCloneCriteria(methodsWithSameMetrics)
            val methodsWithSameNormalizedBody = groupedByCloneCriteria
                .filter { it.size > 1 }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }
}
