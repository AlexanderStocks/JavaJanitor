//package Refactorings
//
//import spoon.processing.AbstractProcessor
//import spoon.reflect.code.*
//import spoon.reflect.declaration.CtClass
//import spoon.reflect.declaration.CtField
//import spoon.reflect.declaration.CtMethod
//import spoon.reflect.declaration.ModifierKind
//import spoon.reflect.reference.CtFieldReference
//import spoon.reflect.reference.CtVariableReference
//import spoon.reflect.visitor.Filter
//import spoon.reflect.visitor.filter.FieldAccessFilter
//import spoon.reflect.visitor.filter.LineFilter
//import spoon.reflect.visitor.filter.TypeFilter
//import spoon.support.reflect.declaration.CtMethodImpl
//
//class RemoveDeadCodeProcessor: AbstractProcessor<CtClass<*>>() {
//    override fun process(ctClass: CtClass<*>) {
//
//        // get all methods that are not private and not constructors
//        val methods = ctClass.methods.filter { method ->
//            method is CtMethodImpl<*> && method.isPrivate
//        }
//
//        // get all field and variable references
//        val fieldReferences = ctClass.getElements(
//            TypeFilter(CtFieldReference::class.java)
//        ).map { it as CtFieldReference<*> }
//
//        val variableReferences = ctClass.getElements(
//            TypeFilter(CtVariableReference::class.java)
//        ).map { it as CtVariableReference<*> }
//
//        // get all used fields and variables
//        val usedFields = fieldReferences.filter { reference ->
//            methods.any { it.body?.getElements(TypeFilter(CtFieldReference::class.java))?.contains(reference) ?: false }
//        }
//
//        val usedVariables = variableReferences.filter { reference ->
//            methods.any { it.body?.getElements(TypeFilter(CtVariableReference::class.java))?.contains(reference) ?: false }
//        }
//
//
//        // remove unused private fields and methods
//        ctClass.declaredFields.filter { field ->
//            field.&& !usedFields.contains(field.reference)
//        }.forEach { ctClass.removeField(it) }
//
//        ctClass.declaredMethods.filter { method ->
//            method is CtMethodImpl<*> && method.isPrivate && !methods.contains(method)
//        }.forEach { ctClass.removeMethod(it) }
//
//        // remove unused private variables
//        ctClass.declaredMethods.forEach { method ->
//            method.body?.let { body ->
//                body.filter { it is CtVariable<?> && it.isPrivate && !usedVariables.contains(it.reference) }
//                    .forEach { body.remove(it) }
//            }
//        }
//    }
//
//    fun isPrivateField(ctField: CtField<*>): Boolean {
//        return ctField.visibility == ModifierKind.PRIVATE
//    }
//}