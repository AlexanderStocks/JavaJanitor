package Metrics

import spoon.reflect.declaration.CtClass

class LinesOfCode {
//    fun calculate(element: CtClass<*>): Int {
//        val startLine = element.position.line
//        val endLine = element.position.endLine
//        return when {
//            endLine - startLine == 0 -> 1
//            else -> endLine - startLine - 1
//        }
//    }

    var linesOfCode = 0

    fun calculate(element: CtClass<*>): Int {
        val startLine = element.getMetadata("startLine")
        val endLine = element.getMetadata("endLine")
        if (startLine != null && endLine != null) {
            linesOfCode += (endLine as Int) - (startLine as Int) + 1
        }
        return linesOfCode
    }
}