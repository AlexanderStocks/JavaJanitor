package refactor

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import refactor.refactorings.removeDuplication.type3Clones.Type3CloneExtractor
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
        val parserConfig = ParserConfiguration()
            .setSymbolResolver(symbolSolver)
        StaticJavaParser.setConfiguration(parserConfig)
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
            }
        }

        //cus.forEach { println(it) }


        println("Refactoring complete")

        // Save the refactored Java files
        //saveRefactoredJavaFiles(cus)

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
            Type3CloneExtractor()
//            ReplaceConcatenationWithStringBuilder()
//            ReplaceUtilityClassesWithSingletons()
//            CollapseNestedIfStatements()
//            RemoveRedundantTernaryOperators()
//            ReplaceForLoopsWithForEach()
//            Type1CloneExtractor()
//            Type2CloneExtractor()
//            Type3CloneExtractor()

        )
    }
}
