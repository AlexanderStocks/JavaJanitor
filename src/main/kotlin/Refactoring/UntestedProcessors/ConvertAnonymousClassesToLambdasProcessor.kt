//package Refactoring.UntestedProcessors
//
//import spoon.processing.AbstractProcessor
//import spoon.reflect.code.CtLambda
//import spoon.reflect.code.CtNewClass
//import spoon.reflect.declaration.CtInterface
//
//class ConvertAnonymousInnerClassesToLambdasProcessor : AbstractProcessor<CtNewClass<*>>() {
//    override fun isToBeProcessed(candidate: CtNewClass<*>): Boolean {
//        val anonymousClass = candidate.anonymousClass ?: return false
//        val superClass = anonymousClass.superclass ?: return false
//        return superClass is CtInterface<*> && anonymousClass.methods.size == 1 && superClass.methods.size == 1
//    }
//
//    override fun process(element: CtNewClass<*>) {
//        val factory = element.factory
//        val anonymousClass = element.anonymousClass
//        val singleMethod = anonymousClass.methods.single()
//
//        val lambda = factory.core().createLambda<CtLambda<*>>(singleMethod).apply {
//            parameters.addAll(singleMethod.parameters)
//            body = singleMethod.body
//        }
//        element.replace(lambda)
//    }
//}