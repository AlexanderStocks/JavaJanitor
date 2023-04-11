package Refactoring.extractClones

import Refactoring.extractClones.CloneTypes.Type1Clones
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod

class ExtractClones : AbstractProcessor<CtClass<*>>() {

    private val normaliser = Normaliser()
    private val type1Clones = Type1Clones()

    override fun process(element: CtClass<*>) {
        element.methods.forEach { println(it.prettyprint()) }
//        val methodAndMetrics = element.methods.map { NormalisedMethodWithMetrics(it) }
//
//        methodAndMetrics.forEach { printNormalisedMethodWithMetrics(it) }
//
//        printResult(type1Clones.find(methodAndMetrics))
    }

    fun printNormalisedMethodWithMetrics(normalisedMethodWithMetrics: NormalisedMethodWithMetrics) {
        val method = normalisedMethodWithMetrics.method
        val methodName = "${method.declaringType.qualifiedName}.${method.simpleName}"
        val normalisedMethodBody = normalisedMethodWithMetrics.normalisedMethod.prettyprint()
        val metrics = normalisedMethodWithMetrics.metrics

        println("Method: $methodName")
        println("Normalized Method Body:")
        println(normalisedMethodBody)
//        println("Metrics:")
//        metrics.forEach { (metric, value) ->
//            println("  - $metric: $value")
//        }
    }

    fun printResult(groups: List<List<CtMethod<*>>>) {
        if (groups.isEmpty()) {
            println("No groups of methods with the same metric values and normalized method bodies were found.")
        } else {
            println("Groups of methods with the same metric values and normalized method bodies:")
            groups.forEachIndexed { index, group ->
                println("Group ${index + 1}:")
                group.forEach { method ->
                    println("  - ${method.declaringType.qualifiedName}.${method.simpleName}")
                }
            }
        }
    }
}