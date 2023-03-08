package Refactoring.Processors

import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtFieldRead
import spoon.reflect.code.CtInvocation
import spoon.reflect.code.CtVariableRead
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtField
import spoon.reflect.declaration.CtVariable
import spoon.reflect.visitor.filter.TypeFilter


class RemoveDeadCodeProcessor : AbstractProcessor<CtClass<*>>() {
    override fun process(ctClass: CtClass<*>) {
        removeDeadVariables(ctClass)
        removeDeadFields(ctClass)
        removeDeadMethods(ctClass)
    }


    private fun removeDeadFields(ctClass: CtClass<*>) {
        val fieldReads = ctClass.getElements(TypeFilter(CtFieldRead::class.java))
            .mapNotNull { it.variable.declaration }
            .toSet()

        ctClass.getElements(TypeFilter(CtField::class.java))
            .filter { it.isPrivate && !fieldReads.contains(it) }
            .forEach { it.delete() }
    }

    private fun removeDeadVariables(ctClass: CtClass<*>) {
        ctClass.methods.forEach { method ->
            val variableReads = method.getElements(TypeFilter(CtVariableRead::class.java))
                .mapNotNull { it.variable.declaration }
                .toSet()

            method.getElements(TypeFilter(CtVariable::class.java))
                .filter { !variableReads.contains(it) }
                .forEach { it.delete() }
        }
    }

    private fun removeDeadMethods(ctClass: CtClass<*>) {
        val methodReads = ctClass.methods
            .flatMap { method -> method.getElements(TypeFilter(CtInvocation::class.java)) }
            .mapNotNull { it.executable }
            .toSet()

        ctClass.methods
            .filter { method -> method.isPrivate && !methodReads.contains(method.reference) }
            .forEach { method -> method.delete() }
    }
}