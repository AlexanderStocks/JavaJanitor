package refactor.refactorings.removeDuplication.common.methodCreation

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.VoidType
import kotlinx.coroutines.runBlocking

class MethodCreator(private val cu: CompilationUnit) {
    private var methodIndex = 0
    private val methods = cu.findAll(MethodDeclaration::class.java)
    private val openAiClient = OpenAiClient()
    private fun getUniqueMethodName(methodBody: String): String {
        val suggestedNames = runBlocking { openAiClient.askGptForMethodNames(methodBody) }
        suggestedNames.forEach { suggestedName ->
            if (methods.none { it.nameAsString == suggestedName }) {
                return suggestedName
            }
        }


        var methodName: String
        do {
            methodName = "genericMethod${methodIndex++}"
        } while (methods.any { it.nameAsString == methodName })
        return methodName
    }

    fun create(cloneGroup: List<MethodDeclaration>): MethodDeclaration {
        // Find the method with the smallest number of statements in its body
        val methodWithSmallestBody = cloneGroup.minBy { it.body.orElse(null)?.statements?.size ?: 0 }

        val genericMethodName = getUniqueMethodName(methodWithSmallestBody.body.get().toString())

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

        // Add the new generic method to the appropriate class or enum in the CompilationUnit.
        val targetClassOrInterface = cu.findFirst(ClassOrInterfaceDeclaration::class.java).orElse(null)
        val targetEnum = cu.findFirst(EnumDeclaration::class.java).orElse(null)

        when {
            targetClassOrInterface != null -> targetClassOrInterface.addMember(genericMethod)
            targetEnum != null -> targetEnum.addMember(genericMethod)
            else -> throw IllegalStateException("No class, interface or enum found in the compilation unit")
        }

        println("Added generic method to the target class or enum: ${targetClassOrInterface?.nameAsString ?: targetEnum?.nameAsString}")


        return genericMethod
    }
}
