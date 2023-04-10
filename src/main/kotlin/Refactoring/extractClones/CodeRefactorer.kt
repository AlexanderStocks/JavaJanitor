//package Refactoring.extractType1Clones
//
//import Refactoring.extractClones.NormalizedCodeFragment
//import spoon.reflect.code.CtReturn
//import spoon.reflect.code.CtStatement
//import spoon.reflect.code.CtStatementList
//import spoon.reflect.declaration.CtClass
//import spoon.reflect.declaration.CtMethod
//import spoon.reflect.declaration.CtParameter
//import spoon.reflect.declaration.ModifierKind
//import spoon.reflect.factory.Factory
//import spoon.reflect.reference.CtTypeReference
//
//class CodeRefactorer {
//
//
//    fun extractRepeatedBehaviorIntoMethod(
//        element: CtClass<*>,
//        clonePairs: List<Pair<NormalizedCodeFragment, NormalizedCodeFragment>>
//    ) {
//        val factory = element.factory
//        val duplicateGroups =
//            clonePairs.map { (fragment1, fragment2) -> listOf(fragment1.statement, fragment2.statement) }
//
//        duplicateGroups.forEachIndexed { index, duplicateGroup ->
//            val returnType = getReturnTypeFromDuplicateGroup(factory, duplicateGroup)
//
//            val extractedMethod = when (returnType.qualifiedName) {
////                "java.lang.Boolean" -> createExtractedMethod<Boolean>(
////                    element,
////                    factory,
////                    duplicateGroup,
////                    returnType,
////                    index
////                )
////
////                "java.lang.Byte" -> createExtractedMethod<Byte>(element, factory, duplicateGroup, returnType, index)
////                "java.lang.Character" -> createExtractedMethod<Char>(
////                    element,
////                    factory,
////                    duplicateGroup,
////                    returnType,
////                    index
////                )
////
////                "java.lang.Double" -> createExtractedMethod<Double>(element, factory, duplicateGroup, returnType, index)
////                "java.lang.Float" -> createExtractedMethod<Float>(element, factory, duplicateGroup, returnType, index)
////                "java.lang.Integer" -> createExtractedMethod<Int>(element, factory, duplicateGroup, returnType, index)
////                "java.lang.Long" -> createExtractedMethod<Long>(element, factory, duplicateGroup, returnType, index)
////                "java.lang.Short" -> createExtractedMethod<Short>(element, factory, duplicateGroup, returnType, index)
//                "java.lang.String" -> createExtractedMethod<String>(element, factory, duplicateGroup, returnType, index)
//                // Add other cases for different types
//                else -> throw Exception("Unsupported return type: ${returnType.qualifiedName}")
//            }
//
//            element.addMethod(extractedMethod)
//
//            duplicateGroup.forEach { statement ->
//                replaceStatementWithMethodCall(statement, extractedMethod)
//            }
//        }
//    }
//
//    private fun getReturnTypeFromDuplicateGroup(
//        factory: Factory,
//        duplicateGroup: List<CtStatement>
//    ): CtTypeReference<*> {
//        val returnType = duplicateGroup.first().let { lastStatement ->
//            if (lastStatement is CtReturn<*>) {
//                lastStatement.returnedExpression.type
//            } else {
//                factory.Type().VOID_PRIMITIVE
//            }
//        }
//        return returnType
//    }
//
//    // Write a when statement that checks the type of the return statement and returns the correct type
//
////    private fun createExtractedMethod(
////        element: CtClass<*>,
////        factory: Factory,
////        duplicateGroup: List<CtStatement>,
////        index: Int
////    ): CtMethod<*> {
////        val modifiers = setOf(ModifierKind.PRIVATE)
////        val returnType = duplicateGroup.first().let { lastStatement ->
////            if (lastStatement is spoon.reflect.code.CtReturn<*>) {
////                lastStatement.returnedExpression.type
////            } else {
////                factory.Type().VOID_PRIMITIVE
////            }
////        }
////        val name = "extractedDuplicateCode${index}"
////        val parameters = emptyList<CtParameter<*>>()
////        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
////        val body = factory.Core().createBlock<CtStatement>()
////
////        duplicateGroup.forEach { stmt ->
////            body.addStatement<CtStatementList>(stmt.clone())
////        }
////
////        return factory.Method().create(element, modifiers, returnType, name, parameters, thrownTypes, body)
////    }
//
//
//    private fun <T> createExtractedMethod(
//        element: CtClass<*>,
//        factory: Factory,
//        duplicateGroup: List<CtStatement>,
//        returnType: CtTypeReference<*>,
//        index: Int
//    ): CtMethod<*> {
//        val modifiers = setOf(ModifierKind.PRIVATE)
//        val name = "extractedDuplicateCode${index}"
//        val parameters = emptyList<CtParameter<*>>()
//        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
//
//        val body = factory.Core().createBlock<T>().apply {
//            duplicateGroup.forEach { stmt ->
//                addStatement<CtStatementList>(stmt.clone())
//            }
//        }
//
//        return factory.Method()
//            .create(element, modifiers, returnType, name, parameters, thrownTypes, body)
//    }
//
//    private fun replaceStatementWithMethodCall(statement: CtStatement, extractedMethod: CtMethod<*>) {
//        val factory = statement.factory
//        val methodCall = factory.Code()
//            .createInvocation(null, extractedMethod.reference)
//
//        statement.replace(methodCall)
//    }
//}
//
