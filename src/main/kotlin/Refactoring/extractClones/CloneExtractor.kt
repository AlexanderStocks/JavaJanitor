package Refactoring.extractClones

import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod

class CloneExtractor {

//    fun createExtractedMethod(clonedMethods: List<CtMethod<*>>): MethodDeclaration {
//        // Choose the base method for extraction (e.g., the first method in the first list)
//        val baseMethod = clonedMethods.first()
//
//        // Convert the base CtMethod object into a JavaParser MethodDeclaration object
//        val sourceCode = baseMethod.prettyprint()
//        val dummyClass = "class DummyClass { $sourceCode }"
//        val compilationUnit = StaticJavaParser.parse(dummyClass)
//        val parsedMethod = compilationUnit.findFirst(MethodDeclaration::class.java).orElse(null)
//
//
//        // Manipulate the MethodDeclaration object as needed
//        // (e.g., rename the method, add parameters, change the return type, etc.)
//
//        return parsedMethod
//    }

    fun extract(element: CtClass<*>, clones: List<List<CtMethod<*>>>) {
        clones.forEach { cloneGroup ->
            val baseMethod = cloneGroup.first().clone()


            val type = baseMethod.type
            element.copyType()
            //element.addMethod < type::class.java, element.copyType()::class.java> (baseMethod)
        }
    }
}