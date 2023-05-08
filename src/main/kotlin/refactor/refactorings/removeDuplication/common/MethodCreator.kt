package refactor.refactorings.removeDuplication.common

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.VoidType

class MethodCreator(private val cu: CompilationUnit, private val clones: List<List<MethodDeclaration>>) {

    fun create() {
        clones.forEach { cloneGroup ->
            if (cloneGroup.isEmpty()) return@forEach

            // Find the method with the smallest number of statements in its body
            val methodWithSmallestBody = cloneGroup.minBy { it.body.get().statements.size }

            val firstMethodName = methodWithSmallestBody.nameAsString
            val genericMethodName = "${firstMethodName}Generic"

            // Create a new generic method with the common code.
            val genericMethod = methodWithSmallestBody.clone()
            genericMethod.setName(genericMethodName)

            println("Created generic method: $genericMethodName")
            println(genericMethodName)


            // Replace the cloned code in each method with a call to the new generic method.
            cloneGroup.forEach { method ->
                val methodCall = MethodCallExpr()
                methodCall.setName(genericMethodName)

                method.parameters.forEach { param ->
                    methodCall.addArgument(NameExpr(param.nameAsString))
                }


                method.body.get().statements.clear()
                if (method.type !is VoidType) {
                    method.body.get().addStatement(ReturnStmt(methodCall))
                } else {
                    method.body.get().addStatement(methodCall)
                }

                println("Replaced method body with call to generic method in method: ${method.nameAsString}")
                println(method)

            }

            // Add the new generic method to the appropriate class in the CompilationUnit.
            val targetClass = cu.findFirst(ClassOrInterfaceDeclaration::class.java).get()

            targetClass.addMember(genericMethod)

            println("Added generic method to the target class: ${targetClass.nameAsString}")
            println(targetClass)
        }
    }
}