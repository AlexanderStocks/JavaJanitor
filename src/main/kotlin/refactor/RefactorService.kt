package refactor

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import refactor.refactorings.collapseNestedIfStatements.CollapseNestedIfStatements
import refactor.refactorings.reformat.Reformat
import refactor.refactorings.removeDuplication.RemoveDuplication
import refactor.refactorings.removeRedundantTernaryOperators.RemoveRedundantTernaryOperators
import refactor.refactorings.replaceConcatentationWithStringBuilder.ReplaceConcatenationWithStringBuilder
import refactor.refactorings.replaceForWithForEach.ReplaceForLoopsWithForEach
import refactor.refactorings.replaceUtilityClassesWithSingletons.ReplaceUtilityClassesWithSingletons
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class RefactorService(projectRootString: String) {
    private val projectRoot: Path = Paths.get(projectRootString)
    private val refactorings: List<Refactoring> = loadRefactorings()


    private val typeSolver = CombinedTypeSolver(
        ReflectionTypeSolver(),
        JavaParserTypeSolver(projectRoot)
    )

    private val symbolSolver = JavaSymbolSolver(typeSolver)

    init {
        println("Initializing JavaParser...")
        val parserConfig = ParserConfiguration()
            .setSymbolResolver(symbolSolver)
        println("JavaParser initialized.")
        StaticJavaParser.setConfiguration(parserConfig)
        println("JavaParser configuration set.")
    }

    fun refactor(): Map<Path, List<String>> {
        println("Parsing Java files...")

        val cus = Files.walk(projectRoot)
            .filter { path -> path.toString().endsWith(".java") }
            .map { path -> StaticJavaParser.parse(path) }
            .collect(Collectors.toList())

        println("parse files")

        cus.forEach { println(it.storage.get().path) }

        val modifiedFiles: MutableMap<Path, MutableList<String>> = mutableMapOf()

        // Apply refactorings
        refactorings.forEach {
            println("Applying ${it.javaClass.simpleName}... ")
            val modifiedPaths = it.process(projectRoot, cus)

            modifiedPaths.forEach { path ->
                modifiedFiles.getOrPut(path) { mutableListOf() }.add(it.javaClass.simpleName)
                println("Modified: $path")
            }
        }

        //cus.forEach { println(it) }


        println("Refactoring complete")

        // Save the refactored Java files
        saveRefactoredJavaFiles(cus)


        return modifiedFiles
    }

    private fun saveRefactoredJavaFiles(cus: List<CompilationUnit>) {
        cus.forEach { cu ->
            val javaFile = projectRoot.resolve(cu.storage.get().path)
            File(javaFile.toUri()).writeText(cu.toString())
        }
    }

    private fun loadRefactorings(): List<Refactoring> {
        return listOf(
            //RecursionToIterationProcessor(),
//            Reformat(),
//            ReplaceConcatenationWithStringBuilder(),
//            ReplaceUtilityClassesWithSingletons(),
            CollapseNestedIfStatements()
//            RemoveRedundantTernaryOperators(),
//            ReplaceForLoopsWithForEach(),
//            RemoveDuplication()
        )
    }
}
