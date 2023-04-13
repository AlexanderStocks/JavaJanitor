package Refactoring.extractClones.CloneTypes

import Refactoring.extractClones.MethodProcessors.ProcessedMethod
import spoon.reflect.declaration.CtMethod

class Type1CloneFinder {
    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<CtMethod<*>>> {


        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }

        val groupsWithSameNormalizedBody = mutableListOf<List<CtMethod<*>>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalizedBody = methodsWithSameMetrics.groupBy { it.normalisedMethod.body.prettyprint() }
            val methodsWithSameNormalizedBody = groupedByNormalizedBody.values.map { methods ->
                methods.map { it.method }
            }.filter { it.size > 1 }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }
}