package Refactoring.extractClones.CloneTypes

import Refactoring.extractClones.MethodProcessors.ProcessedMethod
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import spoon.reflect.declaration.CtMethod

class Type2CloneFinder {

    private val identifierPlaceholder = "ID"
    private val literalPlaceholder = "LITERAL"
    private val typePlaceholder = "TYPE"


    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<CtMethod<*>>> {
        val groupedByMetrics = methodsAndMetrics.groupBy { it.metrics }

        val groupsWithSameNormalizedBody = mutableListOf<List<CtMethod<*>>>()

        groupedByMetrics.values.forEach { methodsWithSameMetrics ->
            val groupedByNormalizedBody = methodsWithSameMetrics.groupBy { method ->
                val sourceCode = method.normalisedMethod.prettyprint()
                val dummyClass = "class DummyClass { $sourceCode }"
                val compilationUnit = StaticJavaParser.parse(dummyClass)
                val parsedMethod = compilationUnit.findFirst(MethodDeclaration::class.java).orElse(null)
                if (parsedMethod != null) {
                    replaceElementsWithPlaceholders(parsedMethod)
                }

                println(parsedMethod.toString())
                parsedMethod.toString()
            }
            val methodsWithSameNormalizedBody = groupedByNormalizedBody.values.map { methods ->
                methods.map { it.method }
            }.filter { it.size > 1 }
            groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
        }

        return groupsWithSameNormalizedBody
    }

    private fun replaceElementsWithPlaceholders(method: MethodDeclaration) {
        method.accept(object : VoidVisitorAdapter<Void>() {

            override fun visit(n: SimpleName, arg: Void?) {
                n.identifier = identifierPlaceholder
                super.visit(n, arg)
            }

            override fun visit(n: StringLiteralExpr, arg: Void?) {
                n.setString(literalPlaceholder)
                super.visit(n, arg)
            }

            override fun visit(n: IntegerLiteralExpr, arg: Void?) {
                n.setValue(literalPlaceholder)
                super.visit(n, arg)
            }

            override fun visit(n: LongLiteralExpr, arg: Void?) {
                n.setValue(literalPlaceholder)
                super.visit(n, arg)
            }

            override fun visit(n: DoubleLiteralExpr, arg: Void?) {
                n.setValue(literalPlaceholder)
                super.visit(n, arg)
            }

            override fun visit(n: CharLiteralExpr, arg: Void?) {
                n.setValue(literalPlaceholder)
                super.visit(n, arg)
            }


            override fun visit(n: ClassOrInterfaceType, arg: Void?) {
                n.name.identifier = typePlaceholder
                super.visit(n, arg)
            }
        }, null)
    }

}
