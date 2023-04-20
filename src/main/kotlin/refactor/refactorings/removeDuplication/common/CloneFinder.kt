package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration

abstract class CloneFinder {

    protected fun findClones(
        methodsAndMetrics: List<ProcessedMethod>,
        groupByNormalizedBody: (List<ProcessedMethod>) -> Map<Any, List<ProcessedMethod>>
    ): List<List<MethodDeclaration>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }
        val groupsWithSameNormalizedBody = mutableListOf<List<MethodDeclaration>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalizedBody = groupByNormalizedBody(methodsWithSameMetrics)
            val methodsWithSameNormalizedBody = groupedByNormalizedBody.values
                .filter { it.size > 1 }
                .map { methods -> methods.map { it.method } }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }
}