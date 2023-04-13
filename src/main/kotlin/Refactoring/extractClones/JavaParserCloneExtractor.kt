package Refactoring.extractClones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.SimpleName
import java.io.File
import java.nio.file.Paths
import java.util.*

fun main() {
    val sourceDirectory = "src/main/resources/TestCases/Type2Clones.java"

    val files = File(sourceDirectory).walk().filter { it.isFile && it.extension == "java" }.toList()

    files.forEach { file ->
        val compilationUnit = StaticJavaParser.parse(file)

        // Process the compilation unit
        processCompilationUnit(compilationUnit)

        // Save the modified compilation unit
        val outputPath = Paths.get("path/to/your/output/directory", file.name)
        println(compilationUnit.toString())
    }
}

fun processCompilationUnit(compilationUnit: CompilationUnit) {
    compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { classDeclaration ->
        val firstMethod = classDeclaration.methods.firstOrNull() ?: return@forEach
        val newMethod = createGenericMethod(firstMethod)
        classDeclaration.addMember(newMethod)
    }
}

fun createGenericMethod(originalMethod: MethodDeclaration): MethodDeclaration {
    val newMethod = originalMethod.clone()

    // Set the generic method name
    newMethod.setName(SimpleName("generic" + originalMethod.nameAsString.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }))

    // Set generic parameter names
    newMethod.parameters.forEachIndexed { index, parameter ->
        parameter.setName("arg$index")
    }

    return newMethod
}
