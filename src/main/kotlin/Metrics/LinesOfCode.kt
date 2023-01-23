package Metrics

import spoon.reflect.declaration.CtClass

class LinesOfCode : Metric {
    override fun calculate(element: CtClass<*>): Int {
        val startLine = element.position.line
        val endLine = element.position.endLine
        return when {
            endLine - startLine == 0 -> 1
            else -> endLine - startLine - 1
        }
    }
}