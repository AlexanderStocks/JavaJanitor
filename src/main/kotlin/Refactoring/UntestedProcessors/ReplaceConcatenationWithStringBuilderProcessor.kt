//package Refactoring.UntestedProcessors
//
//import spoon.processing.AbstractProcessor
//import spoon.reflect.code.CtBinaryOperator
//import spoon.reflect.code.CtFor
//import spoon.reflect.code.BinaryOperatorKind
//import spoon.reflect.code.CtVariableWrite
//
//class ReplaceConcatenationWithStringBuilderProcessor : AbstractProcessor<CtFor>() {
//    override fun isToBeProcessed(candidate: CtFor): Boolean {
//        return candidate.body.descendants.any { it is CtBinaryOperator<*> && it.kind == BinaryOperatorKind.PLUS && it.leftHandOperand.type.isSubtypeOf(candidate.factory.type().createReference(String::class.java)) }
//    }
//
//    override fun process(element: CtFor) {
//        val factory = element.factory
//        val stringBuilderType = factory.type().createReference(StringBuilder::class.java)
//        val stringBuilderVar = factory.createLocalVariable(stringBuilderType, "_stringBuilder", factory.createCodeSnippetExpression("new $stringBuilderType()"))
//        val appendStringBuilder = factory.createCodeSnippetStatement("_stringBuilder.append")
//
//        element.body.statements.forEach { statement ->
//            if (statement is CtBinaryOperator<*> && statement.kind == BinaryOperatorKind.PLUS) {
//                val lhs = statement.leftHandOperand
//                val rhs = statement.rightHandOperand
//
//                val stringBuilderAppendLeft = factory.createInvocation<Void>(factory.createVariableRead(stringBuilderVar.reference, false), stringBuilderType.executableFactory.createReference().setSimpleName("append"), lhs)
//                val stringBuilderAppendRight = factory.createInvocation<Void>(factory.createVariableRead(stringBuilderVar.reference, false), stringBuilderType.executableFactory.createReference().setSimpleName("append"), rhs)
//
//                statement.replace(stringBuilderAppendLeft)
//                stringBuilderAppendLeft.parent.insertAfter(stringBuilderAppendRight)
//            }
//        }
//
//        element.body.insertBefore(stringBuilderVar)
//        element.body.insertAfter(factory.createCodeSnippetStatement("String result = _stringBuilder.toString()"))
//    }
//}