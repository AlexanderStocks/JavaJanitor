package Refactorings

import spoon.processing.AbstractProcessor
import spoon.reflect.code.*
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod
import spoon.reflect.reference.CtVariableReference
import spoon.reflect.visitor.Filter
import spoon.reflect.visitor.filter.LineFilter
import spoon.reflect.visitor.filter.TypeFilter
import spoon.support.reflect.declaration.CtMethodImpl

class RemoveDeadCodeProcessor: AbstractProcessor<CtClass<*>>() {
    override fun process(ctClass: CtClass<*>) {
        val liveLines = mutableSetOf<Int>()
        val deadLines = mutableListOf<Int>()
        // Iterate through all private methods



        ctClass.methods.filter { it.isPrivate && !it.isAbstract }.forEach { method ->
            val methodLiveLines = mutableSetOf<Int>()

            method.body.statements.forEach { statement ->
                if (statement is CtLocalVariable<*>) {
                    val variable = statement.reference
                    if (variable != null && !isVariableUsed(variable, method)) {
                        variable.delete()
                    }
                }
            }

            // Iterate through all statements in the method
            method.body.statements.forEach { statement ->
                // If the statement is an invocation, add the line it's on to the set of live lines
                if (statement is CtInvocation<*>) {
                    methodLiveLines.add(statement.position.line)
                }
            }

            // If the method has live lines, add them to the set of live lines for the class
            if (methodLiveLines.isNotEmpty()) {
                liveLines.addAll(methodLiveLines)
            } else {
                // If the method has no live lines, remove it from the class
                method.delete()
            }
        }

        // Iterate through all private fields
        ctClass.fields.filter { it.isPrivate }.forEach { field ->
            val fieldLiveLines = mutableSetOf<Int>()

            // Iterate through all statements in the class
            ctClass.methods.forEach { method ->
                method.body.statements.forEach { statement ->
                    // If the statement references the field, add the line it's on to the set of live lines
                    if (statement is CtFieldAccess<*>) {
                        if (statement.variable == field.reference) {
                            fieldLiveLines.add(statement.position.line)
                        }
                    }
                }
            }

            // If the field has live lines, add them to the set of live lines for the class
            if (fieldLiveLines.isNotEmpty()) {
                liveLines.addAll(fieldLiveLines)
            } else {
                // If the field has no live lines, remove it from the class
                field.delete()
            }
        }

        // Iterate through all statements in the class and remove any that are not on a live line
        ctClass.methods.forEach { method ->
            method.body.statements.removeAll { statement ->
                statement.position.line !in liveLines
            }
        }
    }

    private fun isVariableUsed(variable: CtVariableReference<*>, method: CtMethod<*>): Boolean {
        val usages = method.body.statements.filter { it is CtVariableRead<*> && it.variable.toString() == variable.toString() }
        return usages.isNotEmpty()
    }
}