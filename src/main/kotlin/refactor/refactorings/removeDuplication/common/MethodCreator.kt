package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.VoidType

class MethodCreator(private val cu: CompilationUnit) {
    private var methodIndex = 0
    private val methods = cu.findAll(MethodDeclaration::class.java)

    private fun getUniqueMethodName(): String {

        var methodName: String
        do {
            methodName = "genericMethod${methodIndex++}"
        } while (methods.any { it.nameAsString == methodName})
        return methodName
    }

    fun create(cloneGroup: List<MethodDeclaration>) : MethodDeclaration {
        // Find the method with the smallest number of statements in its body
        val methodWithSmallestBody = cloneGroup.minBy { it.body.orElse(null)?.statements?.size ?: 0 }

        val genericMethodName = getUniqueMethodName()

        // Create a new generic method with the common code.
        val genericMethod = methodWithSmallestBody.clone()
        genericMethod.setName(genericMethodName)
        genericMethod.annotations?.clear() // Remove annotations

        println("Created generic method: $genericMethodName")
        println(genericMethodName)

        // Replace the cloned code in each method with a call to the new generic method.
        cloneGroup.forEach { method ->
            val methodCall = MethodCallExpr()
            methodCall.setName(genericMethodName)

            method.parameters.forEach { param ->
                methodCall.addArgument(NameExpr(param.nameAsString))
            }

            method.body.orElse(null)?.statements?.clear()
            if (method.type !is VoidType && method.body.isPresent) {
                method.body.get().addStatement(ReturnStmt(methodCall))
            } else {
                method.body.ifPresent { it.addStatement(methodCall) }
            }

            println("Replaced method body with call to generic method in method: ${method.nameAsString}")
        }

        // Add the new generic method to the appropriate class in the CompilationUnit.
        val targetClass = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()

        targetClass.addMember(genericMethod)

        println("Added generic method to the target class: ${targetClass.nameAsString}")

        return genericMethod
    }
}
