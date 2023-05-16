package refactor.refactorings.recursionToIteration

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.stmt.Statement

object RecursionIdentifier {
    fun identifyRecursionTypes(method: MethodDeclaration): Set<RecursionType> {
        val methodName = method.nameAsString
        val methodCalls = method.findAll(MethodCallExpr::class.java)

        val recursionTypes = mutableSetOf<RecursionType>()

        for (methodCall in methodCalls) {
            if (methodCall.nameAsString == methodName) {
                // this is a recursive call
                // analyze the pattern of the recursive call to determine the type of recursion
                // ...

                if (methodCall.isLastStatementInMethod()) {
                    recursionTypes.add(RecursionType.TAIL)
                }

                if (methodCall.hasMultipleRecursiveCalls()) {
                    recursionTypes.add(RecursionType.BINARY)
                }

                if (methodCall.isNestedRecursiveCall()) {
                    recursionTypes.add(RecursionType.NESTED)
                }

                if (methodCall.isMutuallyRecursiveCall()) {
                    recursionTypes.add(RecursionType.MUTUAL)
                }
            }
        }

        if (recursionTypes.isEmpty()) {
            recursionTypes.add(RecursionType.UNKNOWN)
        }

        return recursionTypes
    }

    private fun MethodCallExpr.hasMultipleRecursiveCalls(): Boolean {
        val parentStatement = this.getAncestorOfType(Statement::class.java)
        val methodCalls = parentStatement?.findAll(MethodCallExpr::class.java)

        return (methodCalls?.count { it.nameAsString == this.nameAsString } ?: 0) > 1
    }

    private fun MethodCallExpr.isNestedRecursiveCall(): Boolean {
        val parentStatement = this.getAncestorOfType(Statement::class.java)
        val methodCalls = parentStatement?.findAll(MethodCallExpr::class.java)

        return methodCalls?.any { it.nameAsString == this.nameAsString && it != this } ?: false
    }

    private fun MethodCallExpr.isMutuallyRecursiveCall(): Boolean {
        val parentMethod = this.getAncestorOfType(MethodDeclaration::class.java)
        val siblingMethods = parentMethod?.parentNode?.orElse(null)?.childNodes?.filterIsInstance<MethodDeclaration>()

        return siblingMethods?.any { method ->
            method.nameAsString != parentMethod.nameAsString && method.findAll(MethodCallExpr::class.java)
                .any { it.nameAsString == this.nameAsString }
        } ?: false
    }


    private fun MethodCallExpr.isLastStatementInMethod(): Boolean {
        val parentStatement = this.getAncestorOfType(Statement::class.java)
        val methodBody = this.getAncestorOfType(MethodDeclaration::class.java)?.body?.orElse(null) ?: return false

        return parentStatement == methodBody.statements.last()
    }

    private fun <T : Node> Node.getAncestorOfType(type: Class<T>): T? {
        var current: Node? = this
        while (current != null) {
            if (type.isInstance(current)) {
                @Suppress("UNCHECKED_CAST")
                return current as T
            }
            current = current.parentNode.orElse(null)
        }
        return null
    }
}