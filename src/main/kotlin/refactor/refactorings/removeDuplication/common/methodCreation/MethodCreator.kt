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
        try {
            val suggestedNames = runBlocking { openAiClient.askGptForMethodNames(methodBody) }
            suggestedNames.forEach { suggestedName ->
                if (methods.none { it.nameAsString == suggestedName }) {
                    return suggestedName
                }
            }
        } catch (e: Exception) {
            println("Failed to get method name from GPT-3")
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
        genericMethod.javadocComment.ifPresent { it.remove() }
        genericMethod.setPrivate(true) // Make the method private


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
        }

        // Add the new generic method to the appropriate class or enum in the CompilationUnit.
        val targetClassOrInterface = methodWithSmallestBody.findAncestor(ClassOrInterfaceDeclaration::class.java).orElse(null)
        val targetEnum = methodWithSmallestBody.findAncestor(EnumDeclaration::class.java).orElse(null)

        when {
            targetClassOrInterface != null -> targetClassOrInterface.addMember(genericMethod)
            targetEnum != null -> targetEnum.addMember(genericMethod)
            else -> throw IllegalStateException("No class, interface or enum found in the compilation unit")
        }

        return genericMethod
    }
}
