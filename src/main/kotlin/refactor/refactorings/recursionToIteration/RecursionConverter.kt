package refactor.refactorings.recursionToIteration

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.WhileStmt

class RecursionConverter {

    fun convertRecursionToIteration(cu: CompilationUnit) {
        val allMethodDeclarations = cu.findAll(MethodDeclaration::class.java)
        allMethodDeclarations.forEach { method ->
            val recursionTypes = RecursionIdentifier().identifyRecursionTypes(method)
            if (recursionTypes.isNotEmpty()) {
                transformMethod(method, recursionTypes)
            }
        }

    }

    private fun transformMethod(method: MethodDeclaration, recursionTypes: Set<RecursionType>) {
        if (recursionTypes == setOf(RecursionType.TAIL) && method.isSimpleTailRecursion()) {
            transformTailRecursionToIteration(method)
        }
    }

    private fun transformTailRecursionToIteration(method: MethodDeclaration) {
        val recursiveCall = method.getRecursiveCall() ?: return
        val ifStmt = method.findFirst(IfStmt::class.java).orElse(null) ?: return
        val inverseCondition = ConditionInverter().inverseCondition(ifStmt.condition)

        // Create a while loop that continues as long as the inverse of the recursive condition is true
        val whileStmt = WhileStmt()
        whileStmt.condition = inverseCondition
        whileStmt.body = BlockStmt()

        // Replace the recursive call with a reassignment of the method parameters
        for (i in method.parameters.indices) {
            val param = method.parameters[i]
            val arg = recursiveCall.arguments[i]
            val assignExpr = AssignExpr()
            assignExpr.target = NameExpr(param.nameAsString)
            assignExpr.value = arg.clone()
            assignExpr.operator = AssignExpr.Operator.ASSIGN
            (whileStmt.body as BlockStmt).addStatement(assignExpr)
        }

        // Replace the method body with the while loop
        method.body.get().statements.clear()
        method.body.get().addStatement(whileStmt)
    }

    private fun MethodDeclaration.getRecursiveCall(): MethodCallExpr? {
        val methodName = this.nameAsString

        // Find all method calls in the method body
        val methodCalls = this.findAll(MethodCallExpr::class.java)

        // Return the first method call that matches the method name, if any
        return methodCalls.find { it.nameAsString == methodName }
    }

    private fun MethodDeclaration.isSimpleTailRecursion(): Boolean {
        val recursiveCall = this.getRecursiveCall() ?: return false

        // Find all return statements in the method body
        val returnStmts = this.findAll(ReturnStmt::class.java)

        // Check if there is a return statement that matches the recursive call
        for (returnStmt in returnStmts) {
            val expression = returnStmt.expression.orElse(null)
            if (expression == recursiveCall) {
                return true
            }
        }

        return false
    }
}