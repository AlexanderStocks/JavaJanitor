package refactor.refactorings.removeDeadFieldsMethodsVariables

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import refactor.Refactoring
import java.nio.file.Path

class RemoveDeadCode : Refactoring {

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        return cus.mapNotNull { removeDeadCodeAndSave(it) }
    }

    private fun removeDeadCodeAndSave(cu: CompilationUnit): CompilationUnit? {
        val originalCuString = cu.toString()
        removeDeadCode(cu)
        return if (originalCuString != cu.toString()) cu else null
    }

    private fun removeDeadCode(cu: CompilationUnit) {
        removeDeadMethods(cu)
        removeDeadFields(cu)
        removeDeadVariables(cu)
    }

    private fun removeDeadMethods(cu: CompilationUnit) {
        val allMethodDeclarations = cu.findAll(MethodDeclaration::class.java)
        val allMethodCallExprs = cu.findAll(MethodCallExpr::class.java)

        allMethodDeclarations.filter { it.isPrivate }.filter { methodDeclaration ->
            allMethodCallExprs.none { it.nameAsString == methodDeclaration.nameAsString }
        }.forEach { it.remove() }
    }

    private fun removeDeadFields(cu: CompilationUnit) {
        val allFieldDeclarations = cu.findAll(FieldDeclaration::class.java)

        allFieldDeclarations.filter { it.isPrivate }.filter { fieldDeclaration ->
            val fieldName = fieldDeclaration.variables.first().nameAsString
            cu.findAll(VariableDeclarationExpr::class.java)
                .none { it.variables.any { variable -> variable.nameAsString == fieldName } }
        }.forEach { it.remove() }
    }

    private fun removeDeadVariables(cu: CompilationUnit) {
        cu.findAll(MethodDeclaration::class.java).forEach { methodDeclaration ->
            val allVariableDeclarations = methodDeclaration.findAll(VariableDeclarationExpr::class.java)
            val allVariableNamesUsed = methodDeclaration.findAll(NameExpr::class.java).map { it.nameAsString }.toSet()

            val unusedVariableDeclarations = allVariableDeclarations.filter { variableDeclaration ->
                val variableName = variableDeclaration.variables.first().nameAsString
                !allVariableNamesUsed.contains(variableName)
            }

            unusedVariableDeclarations.forEach { variableDeclaration ->
                variableDeclaration.parentNode.get().remove()
            }
        }
    }

}
