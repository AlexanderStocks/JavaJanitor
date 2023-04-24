//package Metrics.Metric.Classes
//
//import Metrics.Model.Dataset
//import Metrics.Model.Measure
//import Metrics.Model.Metric
//import Metrics.Util.Util
//import spoon.processing.AbstractProcessor
//import spoon.reflect.code.*
//import spoon.reflect.declaration.CtClass
//import spoon.reflect.declaration.CtMethod
//
//class CyclomaticComplexity : AbstractProcessor<CtClass<*>>() {
//    private fun calculateMethodCyclomaticComplexity(ctMethod: CtMethod<*>): Int {
//        return 1 + ctMethod.body.statements.count { statement ->
//            when (statement) {
//                is CtIf, is CtFor, is CtForEach, is CtWhile, is CtDo, is CtConditional<*>, is CtCase<*>, is CtSwitch<*> -> true
//                else -> false
//            }
//        }
//    }
//
//    override fun process(cls: CtClass<*>) {
//        if (Util.isValid(cls)) {
//            val qualifiedName = cls.qualifiedName
//            val methods = cls.methods
//            for (method in methods) {
//                val methodName = method.simpleName
//                val cyclomaticMethodComplexity = calculateMethodCyclomaticComplexity(method).toDouble()
//                Dataset.store("$qualifiedName.$methodName", Measure(Metric.CYCLO, cyclomaticMethodComplexity))
//            }
//        }
//    }
//}
