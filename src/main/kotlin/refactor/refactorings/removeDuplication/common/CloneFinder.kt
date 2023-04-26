package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import java.util.*

abstract class CloneFinder {

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