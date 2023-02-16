package Metrics.Metric

import Metrics.Model.Dataset
import Metrics.Model.Measure
import Metrics.Model.Metric
import Metrics.Util.Util
import spoon.processing.AbstractProcessor
import spoon.reflect.code.*
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod
class CyclomaticComplexity : AbstractProcessor<CtClass<*>>() {

    private fun calculateMethod(ctMethod: CtMethod<*>): Int {
        // Initial complexity is 1 for the method itself
        var complexity = 1

        // Iterate through the method's statements and check for control flow statements
        ctMethod.body.statements.forEach { statement ->
            when (statement) {
                is CtIf, is CtFor, is CtForEach, is CtWhile, is CtDo, is CtConditional<*>, is CtCase<*>, is CtSwitch<*> -> complexity++
            }
        }
        return complexity
    }

//    private fun calculateMethodCyclomaticComplexity(method: CtMethod<*>): Double {
//        var cyclomaticComplexity = 1.0
//        val cfg = method.des
//        if (cfg != null) {
//            cyclomaticComplexity = cfg.edges.filter { it.type == "true" }.count() + 1.0
//        }
//        return cyclomaticComplexity
//    }

    //    override fun process(element: CtClass<*>?) {
//        //return element.getElements(TypeFilter(CtMethod::class.java)).sumOf { calculateMethod(it) }
//    }
    override fun process(cls: CtClass<*>) {
        if (Util.isValid(cls)) {
            val qualifiedName = cls.qualifiedName
            val methods = cls.allMethods
            for (method in methods) {
                val methodName = method.simpleName
//                val cyclomaticComplexity = calculateMethodCyclomaticComplexity(method)
                val cyclomaticComplexity = 7.0
                Dataset.store("$qualifiedName.$methodName", Measure(Metric.CYCLO, cyclomaticComplexity))

            }
        }
    }
}
