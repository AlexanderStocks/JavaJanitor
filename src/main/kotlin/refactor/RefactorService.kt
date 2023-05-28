package refactor

import builder.BuildRunner
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import github.GithubAPI
import kotlinx.coroutines.runBlocking
import org.kohsuke.github.*
import refactor.refactorings.collapseNestedIfStatements.CollapseNestedIfStatements
import refactor.refactorings.reformat.Reformat
import refactor.refactorings.removeDuplication.RemoveDuplication
import refactor.refactorings.removeRedundantTernaryOperators.RemoveRedundantTernaryOperators
import refactor.refactorings.replaceConcatentationWithStringBuilder.ReplaceConcatenationWithStringBuilder
import refactor.refactorings.replaceForWithForEach.ReplaceForLoopsWithForEach
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class RefactorService(private val projectRoot: Path) {
    private val refactorings: List<Refactoring> = loadRefactorings()

    private val typeSolver = CombinedTypeSolver(
        ReflectionTypeSolver(), JavaParserTypeSolver(projectRoot)
    )

    private val symbolSolver = JavaSymbolSolver(typeSolver)

    init {
        initializeJavaParser()
    }

    private fun initializeJavaParser() {
        val parserConfig = ParserConfiguration().setSymbolResolver(symbolSolver)
        StaticJavaParser.setConfiguration(parserConfig)
    }

    fun refactor(
        github: GithubAPI, newBranchName: String
    ): Map<String, List<Path>> {
        val cus = parseJavaFiles()

        return try {
            applyRefactorings(github, newBranchName, cus)
        } catch (e: Exception) {
            e.printStackTrace()
            mapOf()
        }
    }

    private fun parseJavaFiles(): List<CompilationUnit> {

        return Files.walk(projectRoot).filter { path -> path.toString().endsWith(".java") }.map { path ->
            try {
                StaticJavaParser.parse(path)
            } catch (e: Exception) {
                null
            }
        }.collect(Collectors.toList()).filterNotNull()

    }


    private fun applyRefactorings(
        github: GithubAPI, newBranchName: String, cus: List<CompilationUnit>
    ): MutableMap<String, MutableList<Path>> {
        val refactoringMap: MutableMap<String, MutableList<Path>> = mutableMapOf()

        refactorings.forEach { refactoring ->
            println("Applying ${refactoring.javaClass.simpleName}")
            var modifiedCus = refactoring.process(projectRoot, cus)

            if (refactoring !is Reformat) {
                modifiedCus = reformatCus(modifiedCus)
            }

            saveAndRecordChanges(refactoring, modifiedCus, refactoringMap)

            if (modifiedCus.isNotEmpty()) {
                val commitMessage = "Applied ${refactoring.javaClass.simpleName} to ${modifiedCus.size} files"
                val latestCommitSha = github.getRef("heads/$newBranchName").getObject().sha
                val baseTreeSha = github.getTree(latestCommitSha).sha

                createCommitFromRepo(
                    github, commitMessage, modifiedCus, baseTreeSha, latestCommitSha, newBranchName
                )
            }
        }
        return refactoringMap
    }

    private fun reformatCus(modifiedCus: List<CompilationUnit>): List<CompilationUnit> {
        return Reformat().process(projectRoot, modifiedCus)
    }

    private fun saveAndRecordChanges(
        refactoring: Refactoring,
        modifiedCus: List<CompilationUnit>,
        refactoringMap: MutableMap<String, MutableList<Path>>
    ) {
        modifiedCus.forEach { cu ->
            refactoringMap.getOrPut(refactoring.javaClass.simpleName) { mutableListOf() }.add(cu.storage.get().path)

            cu.storage.get().save()
        }
    }

    private fun createCommitFromRepo(
        githubAPI: GithubAPI,
        commitMessage: String,
        modifiedCus: List<CompilationUnit>,
        baseTreeSha: String,
        parentCommitSha: String,
        newBranchName: String
    ) {
        // Step 1: Create blobs for the file contents
        val treeBuilder = githubAPI.createTree().baseTree(baseTreeSha)

        modifiedCus.forEach { cu ->
            val content = cu.toString()
            val path = cu.storage.map { projectRoot.relativize(it.path) }.orElse(null)
                ?: throw RuntimeException("Failed to get path for CompilationUnit")

            treeBuilder.add(path.toString().replace("\\", "/"), content, false)
        }

        val tree: GHTree
        try {
            tree = treeBuilder.create()
        } catch (e: IOException) {
            throw RuntimeException("Failed to create tree", e)
        }

        // Step 3: Create the commit
        val commit = runBlocking {
            githubAPI.createCommit(
                commitMessage,
                tree.sha,
                parentCommitSha,
            )
        }

        // Update the branch to point to the new commit
        try {
            val ref = githubAPI.getRef("heads/$newBranchName")
            ref.updateTo(commit.sha, true)
        } catch (e: IOException) {
            throw RuntimeException("Failed to update branch reference", e)
        }
    }


    private fun loadRefactorings(): List<Refactoring> {
        return listOf(
            //RecursionToIterationProcessor(),
            Reformat(),
            ReplaceConcatenationWithStringBuilder(),
            CollapseNestedIfStatements(),
            RemoveRedundantTernaryOperators(),
            ReplaceForLoopsWithForEach(),
            RemoveDuplication()
        )
    }
}
