package refactor.refactorings.replaceUtilityClassesWithSingletons

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.stmt.BlockStmt
import refactor.Refactoring
import java.nio.file.Path

class ReplaceUtilityClassesWithSingletons : Refactoring {
    override fun process(cus: List<CompilationUnit>): List<Path> {
        return cus.mapNotNull { replaceUtilityClassesWithSingletonsAndSave(it) }
    }

    private fun replaceUtilityClassesWithSingletonsAndSave(cu: CompilationUnit): Path? {
        val originalCuString = cu.toString()
        replaceUtilityClassesWithSingletons(cu)
        return if (originalCuString != cu.toString()) cu.storage.get().path else null
    }

    private fun replaceUtilityClassesWithSingletons(cu: CompilationUnit) {
        val utilityClasses = cu.findAll(ClassOrInterfaceDeclaration::class.java).filter { isUtilityClass(it) }

        utilityClasses.forEach { utilityClass ->
            // Make the constructor private
            val defaultConstructor = utilityClass.defaultConstructor.orElseGet {
                val constructor = utilityClass.addConstructor(Modifier.Keyword.PRIVATE)
                constructor
            }
            defaultConstructor.setPrivate(true)

            // Add a private static final instance field

            val fieldType = StaticJavaParser.parseClassOrInterfaceType(utilityClass.nameAsString)
            val variableDeclaration = VariableDeclarator(fieldType, "INSTANCE")
            variableDeclaration.setInitializer("new ${utilityClass.nameAsString}()")

            utilityClass.addField(
                utilityClass.nameAsString,
                "INSTANCE",
                Modifier.Keyword.PRIVATE,
                Modifier.Keyword.STATIC,
                Modifier.Keyword.FINAL
            ).variables[0] = variableDeclaration

            // Add a public static getInstance() method
            val getInstanceMethod =
                utilityClass.addMethod("getInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            getInstanceMethod.setType(utilityClass.nameAsString)
            getInstanceMethod.setBody(BlockStmt().addStatement("return INSTANCE;"))
        }
    }

    private fun isUtilityClass(classDeclaration: ClassOrInterfaceDeclaration): Boolean {
        if (classDeclaration.isInterface || classDeclaration.isAbstract) return false

        val constructors = classDeclaration.constructors
        if (constructors.any { it.isPublic || it.isProtected }) return false

        val methods = classDeclaration.methods
        return methods.isNotEmpty() && methods.all { it.isStatic }
    }
}
