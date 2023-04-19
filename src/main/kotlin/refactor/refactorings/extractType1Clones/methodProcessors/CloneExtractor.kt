package refactor.refactorings.extractType1Clones.methodProcessors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr

class CloneExtractor(private val cu: CompilationUnit, private val clones: List<List<MethodDeclaration>>) {

    fun extract() {
        clones.forEach { cloneGroup ->
            if (cloneGroup.isEmpty()) return@forEach


            // Create a new generic method with the common code.
            val genericMethodName = "genericMethod" // Choose a suitable name
            val genericMethod = cloneGroup.first().clone()
            genericMethod.setName(genericMethodName)

            println("Created generic method: $genericMethodName")
            println(genericMethodName)


            // Replace the cloned code in each method with a call to the new generic method.
            cloneGroup.forEach { method ->
                val methodCall = MethodCallExpr()
                methodCall.setName(genericMethodName)
                method.body.get().statements.clear()
                method.body.get().addStatement(methodCall)

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