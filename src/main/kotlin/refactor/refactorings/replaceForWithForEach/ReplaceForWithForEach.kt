package refactor.refactorings.replaceForWithForEach

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.*
import refactor.Refactoring
import java.nio.file.Path

class ReplaceForLoopsWithForEach : Refactoring {
    override fun process(cus: List<CompilationUnit>): List<Path> {
        return cus.mapNotNull { replaceForLoopsWithForEachAndSave(it) }
    }

    private fun replaceForLoopsWithForEachAndSave(cu: CompilationUnit): Path? {
        val originalCuString = cu.toString()
        replaceForLoopsWithForEach(cu)

        return if (originalCuString != cu.toString()) cu.storage.get().path else null
    }

    private fun replaceForLoopsWithForEach(cu: CompilationUnit) {
        val forLoops = cu.findAll(ForStmt::class.java)

        forLoops.forEach { forLoop ->
            if (forLoop.initialization.size == 1 && forLoop.update.size == 1) {
                val init = forLoop.initialization[0] as? VariableDeclarationExpr
                val update = forLoop.update[0] as? UnaryExpr

                if (init?.variables?.size == 1 && update != null && update.operator == UnaryExpr.Operator.POSTFIX_INCREMENT) {
                    val variableName = init.variables[0].nameAsString
                    val indexName = update.expression.toString()

                    if (variableName == indexName) {
                        val iterableExpr = extractIterableExpression(forLoop.compare.get(), indexName)
                        if (iterableExpr != null) {
                            val forEachVariable = init.clone()
                            forEachVariable.variables[0].removeInitializer()

                            val forEachStmt = ForEachStmt(
                                forEachVariable,
                                iterableExpr,
                                forLoop.body.clone()
                            )

                            forLoop.replace(forEachStmt)
                        }
                    }
                }
            }
        }
    }

    private fun extractIterableExpression(compare: Expression, indexName: String): Expression? {
        if (compare is BinaryExpr && compare.operator == BinaryExpr.Operator.LESS) {
            val left = compare.left
            val right = compare.right

            if (left is NameExpr && left.nameAsString == indexName && right is FieldAccessExpr) {
                val iterable = right.scope
                if (iterable is NameExpr && right.nameAsString == "length") {
                    return iterable
                }
            }
        }
        return null
    }
}
