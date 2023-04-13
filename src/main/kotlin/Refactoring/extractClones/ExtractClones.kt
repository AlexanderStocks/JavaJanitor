package Refactoring.extractClones

import Refactoring.extractClones.CloneTypes.Type1CloneFinder
import Refactoring.extractClones.CloneTypes.Type2CloneFinder
import Refactoring.extractClones.MethodProcessors.ProcessedMethod
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod

class ExtractClones : AbstractProcessor<CtClass<*>>() {

    private val type1CloneFinder = Type1CloneFinder()
    private val type2CloneFinder = Type2CloneFinder()
    private val cloneExtractor = CloneExtractor()

    override fun process(element: CtClass<*>) {
        val processedMethods = element.methods.map { ProcessedMethod(it) }

        //processedMethods.forEach { println(it.normalisedMethod.prettyprint()) }

        val type1Clones = type1CloneFinder.find(processedMethods)
        cloneExtractor.extract(element, type1Clones)
    }

    fun printNormalisedMethodWithMetrics(processedMethod: ProcessedMethod) {
        val method = processedMethod.method
        val methodName = "${method.declaringType.qualifiedName}.${method.simpleName}"
        val normalisedMethodBody = processedMethod.normalisedMethod.prettyprint()
        val metrics = processedMethod.metrics

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