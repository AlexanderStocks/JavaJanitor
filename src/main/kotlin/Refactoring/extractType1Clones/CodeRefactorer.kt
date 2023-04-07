package Refactoring.extractType1Clones

import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtStatementList
import spoon.reflect.declaration.*
import spoon.reflect.factory.Factory
import spoon.reflect.reference.CtTypeReference

class CodeRefactorer {
    fun extractRepeatedBehaviorIntoMethod(
        element: CtClass<*>,
        clonePairs: List<Pair<NormalizedCodeFragment, NormalizedCodeFragment>>
    ) {
        val factory = element.factory
        val duplicateGroups =
            clonePairs.map { (fragment1, fragment2) -> listOf(fragment1.statement, fragment2.statement) }

        duplicateGroups.forEachIndexed { index, duplicateGroup ->
            val extractedMethod = createExtractedMethod<Any>(element, factory, duplicateGroup, index)

            element.addMethod(extractedMethod as CtMethod<CtType<Any>>)

            duplicateGroup.forEach { statement ->
                replaceStatementWithMethodCall(statement, extractedMethod)
            }
        }
    }

//    private fun createExtractedMethod(
//        element: CtClass<*>,
//        factory: Factory,
//        duplicateGroup: List<CtStatement>,
//        index: Int
//    ): CtMethod<*> {
//        val modifiers = setOf(ModifierKind.PRIVATE)
//        val returnType = duplicateGroup.first().let { lastStatement ->
//            if (lastStatement is spoon.reflect.code.CtReturn<*>) {
//                lastStatement.returnedExpression.type
//            } else {
//                factory.Type().VOID_PRIMITIVE
//            }
//        }
//        val name = "extractedDuplicateCode${index}"
//        val parameters = emptyList<CtParameter<*>>()
//        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
//        val body = factory.Core().createBlock<CtStatement>()
//
//        duplicateGroup.forEach { stmt ->
//            body.addStatement<CtStatementList>(stmt.clone())
//        }
//
//        return factory.Method().create(element, modifiers, returnType, name, parameters, thrownTypes, body)
//    }

    private inline fun <reified T> createExtractedMethod(
        element: CtClass<*>,
        factory: Factory,
        duplicateGroup: List<CtStatement>,
        index: Int
    ): CtMethod<*> {
        val modifiers = setOf(ModifierKind.PRIVATE)
        val name = "extractedDuplicateCode${index}"
        val parameters = emptyList<CtParameter<*>>()
        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
        val returnType = T::class.java
        val body = factory.Core().createBlock<T>()

        duplicateGroup.forEach { stmt ->
            body.addStatement<CtStatementList>(stmt.clone())
        }

        return factory.Method()
            .create(element, modifiers, factory.Type().createReference(returnType), name, parameters, thrownTypes, body)
    }

    private fun replaceStatementWithMethodCall(statement: CtStatement, extractedMethod: CtMethod<*>) {
        val factory = statement.factory
        val methodCall = factory.Code()
            .createInvocation(null, extractedMethod.reference)

        statement.replace(methodCall)
    }
}

