package refactor.refactorings.removeDuplication.common.cloneFinders

import refactor.refactorings.removeDuplication.common.ProcessedMethod

abstract class BaseCloneFinder : CloneFinder {

    protected fun findClones(
        methodsAndMetrics: List<ProcessedMethod>,
        groupByCloneCriteria: (List<ProcessedMethod>) -> List<List<ProcessedMethod>>
    ): List<List<ProcessedMethod>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }
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
