//package Refactoring.Processors
//
//import spoon.processing.AbstractProcessor
//import spoon.reflect.code.*
//import spoon.reflect.declaration.CtClass
//import spoon.reflect.declaration.CtMethod
//import spoon.reflect.declaration.CtParameter
//import spoon.reflect.declaration.ModifierKind
//import spoon.reflect.factory.Factory
//import spoon.reflect.reference.CtTypeReference
//import spoon.reflect.visitor.filter.TypeFilter
//
//class ExtractDuplicatedCodeProcessor : AbstractProcessor<CtClass<*>>() {
//    //    private fun normalizeBlock(block: CtBlock<*>): String {
////        return block.statements.joinToString("\n") { it.toString().trim() }
////    }
//    private fun normalizeStatements(statements: List<CtStatement>): String {
//        return statements.joinToString("\n") { it.toString().trim() }
//    }
//
//
////    private fun findDuplicateCode(blocks: List<CtBlock<*>>): List<List<CtBlock<*>>> {
////        val duplicates = mutableListOf<List<CtBlock<*>>>()
////        val assignedBlocks = mutableSetOf<CtBlock<*>>()
////
////        for (i in blocks.indices) {
////            if (assignedBlocks.contains(blocks[i])) {
////                continue
////            }
////
////            val currentBlock = blocks[i]
////            val normalizedCurrentBlock = normalizeBlock(currentBlock)
////            val duplicateGroup = mutableListOf<CtBlock<*>>()
////
////            for (j in (i + 1) until blocks.size) {
////                val nextBlock = blocks[j]
////                val normalizedNextBlock = normalizeBlock(nextBlock)
////
////                if (normalizedCurrentBlock == normalizedNextBlock) {
////                    if (!assignedBlocks.contains(nextBlock)) {
////                        if (duplicateGroup.isEmpty()) {
////                            duplicateGroup.add(currentBlock)
////                            assignedBlocks.add(currentBlock)
////                        }
////                        duplicateGroup.add(nextBlock)
////                        assignedBlocks.add(nextBlock)
////                    }
////                }
////            }
////
////            if (duplicateGroup.isNotEmpty()) {
////                duplicates.add(duplicateGroup)
////            }
////        }
////
////        return duplicates
////    }
//
//    private fun findDuplicateCode(blocks: List<CtBlock<*>>): List<List<List<CtStatement>>> {
//        val duplicates = mutableListOf<List<List<CtStatement>>>()
//        val assignedStatements = mutableSetOf<CtStatement>()
//
//        for (block in blocks) {
//            val statements = block.statements
//            for (i in statements.indices) {
//                if (assignedStatements.contains(statements[i])) {
//                    continue
//                }
//
//                val currentStatements = mutableListOf(statements[i])
//                val normalizedCurrentStatements = normalizeStatements(currentStatements)
//                val duplicateGroup = mutableListOf<List<CtStatement>>()
//
//                for (nextBlock in blocks) {
//                    if (block == nextBlock) continue
//                    val nextStatements = nextBlock.statements
//                    for (j in nextStatements.indices) {
//                        if (assignedStatements.contains(nextStatements[j])) {
//                            continue
//                        }
//
//                        val nextStatementsSubset = nextStatements.subList(j, nextStatements.size)
//                        if (normalizeStatements(nextStatementsSubset).startsWith(normalizedCurrentStatements)) {
//                            if (duplicateGroup.isEmpty()) {
//                                duplicateGroup.add(currentStatements)
//                                currentStatements.forEach { assignedStatements.add(it) }
//                            }
//                            duplicateGroup.add(nextStatementsSubset.subList(0, currentStatements.size))
//                            nextStatementsSubset.subList(0, currentStatements.size)
//                                .forEach { assignedStatements.add(it) }
//                        }
//                    }
//                }
//
//                if (duplicateGroup.isNotEmpty()) {
//                    duplicates.add(duplicateGroup)
//                }
//            }
//        }
//
//        return duplicates
//    }
//
////    private fun createExtractedMethod(
////        element: CtClass<*>,
////        factory: Factory,
////        duplicateGroup: List<CtBlock<*>>,
////        index: Int
////    ): CtMethod<*> {
////        val modifiers = setOf(ModifierKind.PRIVATE)
////        val returnType = factory.Type().VOID_PRIMITIVE
////        val name = "extractedDuplicateCode${index}"
////        val parameters = emptyList<CtParameter<*>>()
////        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
////        val body = factory.Core().createBlock<Void>()
////
////        duplicateGroup.first().statements.forEach { stmt ->
////            body.addStatement<CtStatementList>(stmt.clone())
////        }
////
////        return factory.Method().create(element, modifiers, returnType, name, parameters, thrownTypes, body)
////    }
//
//    private fun createExtractedMethod(
//        element: CtClass<*>,
//        factory: Factory,
//        duplicateGroup: List<List<CtStatement>>,
//        index: Int
//    ): CtMethod<*> {
//        val modifiers = setOf(ModifierKind.PRIVATE)
//        val returnType = duplicateGroup.first().last().let { lastStatement ->
//            if (lastStatement is CtReturn<*>) {
//                lastStatement.returnedExpression.type
//            } else {
//                factory.Type().VOID_PRIMITIVE
//            }
//        }
//        val name = "extractedDuplicateCode${index}"
//        val parameters = emptyList<CtParameter<*>>()
//        val thrownTypes = emptySet<CtTypeReference<out Throwable>>()
//        val body = factory.Core().createBlock<Any>()
//
//        duplicateGroup.first().forEach { stmt ->
//            body.addStatement<CtStatementList>(stmt.clone())
//        }
//
//        return factory.Method().create(element, modifiers, returnType, name, parameters, thrownTypes, body)
//    }
//
////    private fun replaceDuplicateCodeWithMethodCall(duplicateGroup: List<CtBlock<*>>, extractedMethod: CtMethod<*>) {
////        val executable = factory.Executable().createReference(extractedMethod)
////        val arguments = emptyList<CtExpression<*>>()
////        duplicateGroup.forEach { duplicateBlock ->
////            val factory = duplicateBlock.factory
////            val methodCall = factory.Code()
////                .createInvocation(null, executable, arguments)
////
////            println(methodCall)
////            println(duplicateBlock.parent)
////            duplicateBlock.replace(factory.createCtBlock(methodCall))
////        }
////    }
//
//
////    override fun process(element: CtClass<*>) {
////        val allBlocks = element.getElements(TypeFilter(CtBlock::class.java))
////        val duplicateGroups = findDuplicateCode(allBlocks)
////
////        duplicateGroups.forEachIndexed { index, duplicateGroup ->
////            val extractedMethod = createExtractedMethod(element, element.factory, duplicateGroup, index + 1)
////
////            replaceDuplicateCodeWithMethodCall(duplicateGroup, extractedMethod)
////        }
////    }
//
//    private fun replaceDuplicateCodeWithMethodCall(
//        duplicateGroup: List<List<CtStatement>>,
//        extractedMethod: CtMethod<*>
//    ) {
//        val executable = factory.Executable().createReference(extractedMethod)
//        val arguments = emptyList<CtExpression<*>>()
//
//        duplicateGroup.forEach { duplicateStatements ->
//            val factory = duplicateStatements.first().factory
//            val methodCall = factory.Code()
//                .createInvocation(null, executable, arguments)
//
//            val statementToReplace = duplicateStatements.first()
//            statementToReplace.replace(methodCall)
//            duplicateStatements.drop(1).forEach { it.delete() }
//        }
//    }
//
//    override fun process(element: CtClass<*>) {
//        val allBlocks = element.getElements(TypeFilter(CtBlock::class.java))
//        val duplicateGroups = findDuplicateCode(allBlocks)
//
//        duplicateGroups.forEachIndexed { index, duplicateGroup ->
//            val extractedMethod = createExtractedMethod(element, element.factory, duplicateGroup, index + 1)
//
//            replaceDuplicateCodeWithMethodCall(duplicateGroup, extractedMethod)
//        }
//    }
//}