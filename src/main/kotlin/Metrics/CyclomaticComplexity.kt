package Metrics

import spoon.reflect.code.*
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.filter.TypeFilter

class CyclomaticComplexity : Metric {
    override fun calculate(element: CtClass<*>): Int {
        return element.getElements(TypeFilter(CtMethod::class.java)).sumOf { calculateMethod(it) }
    }

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
}