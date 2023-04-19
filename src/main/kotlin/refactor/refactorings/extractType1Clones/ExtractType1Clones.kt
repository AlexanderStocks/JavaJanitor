package refactor.refactorings.extractType1Clones

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.Refactoring
import refactor.refactorings.extractType1Clones.methodProcessors.CloneExtractor
import refactor.refactorings.extractType1Clones.methodProcessors.CloneFinder
import refactor.refactorings.extractType1Clones.methodProcessors.ProcessedMethod
import java.nio.file.Path

class ExtractType1Clones : Refactoring() {
    private val cloneFinder = CloneFinder()
    override fun process(cus: List<CompilationUnit>): List<Path> {
        val modifiedFiles = mutableSetOf<Path>()
        cus.forEach { cu ->
            val methods = cu.findAll(MethodDeclaration::class.java)
            val processedMethods = methods.map { ProcessedMethod(it) }
            val type1Clones = cloneFinder.find(processedMethods)

            if (type1Clones.isNotEmpty()) {
                CloneExtractor(cu, type1Clones).extract()
                modifiedFiles.add(cu.storage.get().path)
            }

        }
        return modifiedFiles.toList()
    }


    private fun printResults(groups: List<List<MethodDeclaration>>) {
        groups.forEachIndexed { groupIndex, group ->
            println("Group ${groupIndex + 1}:")
            group.forEachIndexed { methodIndex, method ->
                println("\tMethod ${methodIndex + 1}:")
                println("\t\tName: ${method.nameAsString}")
                println("\t\tSignature: ${method.signature}")
                println("\t\tParameters: ${method.parameters}")
                println("\t\tBody:")
                method.body.ifPresent { body ->
                    body.toString().split("\n").forEach { line ->
                        println("\t\t\t$line")
                    }
                }
                println()
            }
        }
    }
}