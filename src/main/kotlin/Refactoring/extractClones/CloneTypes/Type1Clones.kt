package Refactoring.extractClones.CloneTypes

import Refactoring.extractClones.NormalisedMethodWithMetrics
import spoon.reflect.declaration.CtMethod

class Type1Clones {
    fun find(methodsAndMetrics: List<NormalisedMethodWithMetrics>): List<List<CtMethod<*>>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }
        val groupsWithSameNormalizedBody = mutableListOf<List<CtMethod<*>>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalizedBody = methodsWithSameMetrics.groupBy { it.normalisedMethod.prettyprint() }
            val methodsWithSameNormalizedBody = groupedByNormalizedBody.values.map { methods ->
                methods.map { it.method }
            }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }
}