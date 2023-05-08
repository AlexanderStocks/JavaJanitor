package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod

abstract class MetricCloneFinder : CloneFinder {

    protected fun findClones(
        methodsAndMetrics: List<ProcessedMethod>,
        groupByNormalizedBody: (List<ProcessedMethod>) -> List<List<ProcessedMethod>>
    ): List<List<MethodDeclaration>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }
        val groupsWithSameNormalizedBody = mutableListOf<List<MethodDeclaration>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalizedBody = groupByNormalizedBody(methodsWithSameMetrics)
            val methodsWithSameNormalizedBody = groupedByNormalizedBody
                .filter { it.size > 1 }
                .map { methods -> methods.map { it.method } }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }
}