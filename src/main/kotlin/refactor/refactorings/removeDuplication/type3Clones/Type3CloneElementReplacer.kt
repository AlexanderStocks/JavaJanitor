package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.ConditionalExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.DoStmt
import com.github.javaparser.ast.stmt.ForStmt
import com.github.javaparser.ast.stmt.IfStmt
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer
import kotlin.jvm.optionals.getOrElse

object Type3CloneElementReplacer {
    private val javaParser = JavaParser()

    fun replace(method: MethodDeclaration) {
        // Rename variables to a standard format
        Type2CloneElementReplacer.replace(method)

        // Normalize loops and conditionals
        // Replace multiple declarations with single-line declarations

        method.walk { node: Node ->
            when (node) {
                is ForStmt -> node.replaceWithWhileStmt()
                is DoStmt -> node.replaceWithWhileStmt()
                is IfStmt -> node.replaceElseIfWithNestedIf()
                is ConditionalExpr -> node.replaceWithIfStmt()
                is VariableDeclarationExpr -> node.combineDeclarations()
            }
        }

        println("Method body after replacing elements: ${method.body}")

        // Replace braces
        method.removeBraces()
    }

    private fun ForStmt.replaceWithWhileStmt() {
        // Replace for loop with while loop
    }

    private fun DoStmt.replaceWithWhileStmt() {
        // Replace do-while loop with while loop
    }

    private fun IfStmt.replaceElseIfWithNestedIf() {
        if (elseStmt.isPresent) {
            val elseStatement = elseStmt.get()
            if (elseStatement is IfStmt) {
                elseStatement.replaceElseIfWithNestedIf()
                val nestedIf =
                    IfStmt(
                        elseStatement.condition,
                        elseStatement.thenStmt,
                        elseStatement.elseStmt.getOrElse { null }?.clone()
                    )
                val blockStmt = BlockStmt(NodeList(nestedIf))
                setElseStmt(blockStmt)
            }
        }
    }

    private fun ConditionalExpr.replaceWithIfStmt() {
//        // Replace ternary conditional expressions with if statements
//        val tempVarName = "tempResult"
//
//        // Create a temporary CompilationUnit
//        val tempCompilationUnit = CompilationUnit()
//        tempCompilationUnit.addType(ClassOrInterfaceDeclaration().setName("TempClass"))
//            .addMethod("tempMethod", Modifier.Keyword.PUBLIC)
//            .addAndGetBody().addAndGetStatement(this)
//
//        val commonResolvedType = thenExpr.calculateResolvedType()
//        val commonType = javaParser.parseType(commonResolvedType.describe())
//
//
//        val tempVarDeclaration =
//            ExpressionStmt(VariableDeclarationExpr(VariableDeclarator(commonType.result.get(), tempVarName)))
//
//        val thenAssign = AssignExpr(NameExpr(tempVarName), thenExpr, AssignExpr.Operator.ASSIGN)
//        val elseAssign = AssignExpr(NameExpr(tempVarName), elseExpr, AssignExpr.Operator.ASSIGN)
//
//        val ifStmt = IfStmt(condition, ExpressionStmt(thenAssign), ExpressionStmt(elseAssign))
//        val blockStmt = BlockStmt(NodeList(tempVarDeclaration, ifStmt))
//
//        replace(NameExpr(tempVarName))
//        parentNode.get().also { parent ->
//            if (parent is BlockStmt) {
//                parent.addStatement(
//                    parent.statements.indexOfFirst { it is ExpressionStmt && it.expression == this },
//                    blockStmt
//                )
//            } else {
//                val newBlockStmt = BlockStmt()
//                parent.replace(newBlockStmt)
//                newBlockStmt.addStatement(blockStmt)
//                newBlockStmt.addStatement(ExpressionStmt(this))
//            }
//        }
    }

    private fun MethodDeclaration.removeBraces() {
        // Remove braces in the method body
    }

    private fun VariableDeclarationExpr.combineDeclarations() {
        // Combine multiple variable declarations into single-line declarations
    }
}