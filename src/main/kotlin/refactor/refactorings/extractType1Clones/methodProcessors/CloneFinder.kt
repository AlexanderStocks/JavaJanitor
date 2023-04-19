package refactor.refactorings.extractType1Clones.methodProcessors

import com.github.javaparser.ast.body.MethodDeclaration

class CloneFinder {
    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }

        val groupsWithSameNormalisedBody = mutableListOf<List<MethodDeclaration>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalisedBody =
                methodsWithSameMetrics.groupBy { it.normalisedMethod.body.map { body -> body.toString() }.orElse("") }
            val methodsWithSameNormalisedBody =
                groupedByNormalisedBody.values.map { methods -> methods.map { it.method } }.filter { it.size > 1 }
            groupsWithSameNormalisedBody.addAll(methodsWithSameNormalisedBody)
        }

        return groupsWithSameNormalisedBody
    }
}