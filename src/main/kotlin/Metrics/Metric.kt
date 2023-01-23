package Metrics

import spoon.reflect.declaration.CtClass

interface Metric {
    fun calculate(element: CtClass<*>): Int
}