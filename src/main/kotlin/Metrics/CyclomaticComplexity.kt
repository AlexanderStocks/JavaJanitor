package Metrics

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

    override fun process(element: CtClass<*>?) {
        //return element.getElements(TypeFilter(CtMethod::class.java)).sumOf { calculateMethod(it) }
    }
}