package Metrics.Metric

import Metrics.Model.Dataset
import Metrics.Model.Measure
import Metrics.Model.Metric
import Metrics.Util.Util
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass

class LinesOfCode : AbstractProcessor<CtClass<*>>(){
//    fun calculate(element: CtClass<*>): Int {
//        val startLine = element.position.line
//        val endLine = element.position.endLine
//        return when {
//            endLine - startLine == 0 -> 1
//            else -> endLine - startLine - 1
//        }
//    }

    override fun process(element: CtClass<*>) {
        if (Util.isValid(element)) {
            val qualifiedName = element.qualifiedName
            val startLine = element.position.line
            val endLine = element.position.endLine
            val totalLoC = if (endLine - startLine == 0) 1.0 else (endLine - startLine - 1).toDouble()
            Dataset.store(qualifiedName, Measure(Metric.LoC, totalLoC))
        }
    }
}