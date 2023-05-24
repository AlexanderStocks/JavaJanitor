package refactor

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

    fun refactor(
        github: GithubAPI,
        ghRepository: GHRepository,
        newBranchName: String
    ): Map<String, List<Path>> {
        println("Parsing Java files...")

        val cus = Files.walk(projectRoot)
            .filter { path -> path.toString().endsWith(".java") }
            .map { path ->
                try {
                    StaticJavaParser.parse(path)
                } catch (e: Exception) {
                    println("Failed to parse $path")
                    throw IllegalArgumentException("Failed to parse $path", e)
                }
            }
            .collect(Collectors.toList())

        val refactoringMap: MutableMap<String, MutableList<Path>> = mutableMapOf()

        // Apply refactorings
        refactorings.forEach { refactoring ->
            println("Applying ${refactoring.javaClass.simpleName}... ")
            val modifiedCus = refactoring.process(projectRoot, cus)

            // After each refactoring, save the modified files, commit the changes, and push to GitHub
            modifiedCus.forEach { cu ->
                refactoringMap.getOrPut(refactoring.javaClass.simpleName) { mutableListOf() }.add(cu.storage.get().path)

                // Save the refactored Java files
                cu.storage.get().save()
                println("Saved modified: ${cu.storage.get().path}")
            }

            if (modifiedCus.isNotEmpty()) {
                // Create a commit for the refactoring
                val commitMessage = "Applied ${refactoring.javaClass.simpleName} to ${modifiedCus.size} files"
                val latestCommitSha = ghRepository.getRef("heads/$newBranchName").getObject().sha
                println("Latest commit: $latestCommitSha")
                val baseTreeSha = ghRepository.getTree(latestCommitSha).sha
                println("Base tree: $baseTreeSha")

                createCommitFromRepo(
                    github,
                    ghRepository,
                    commitMessage,
                    modifiedCus,
                    baseTreeSha,
                    latestCommitSha,
                    newBranchName
                )
            }

        }

        println("Refactoring complete")
        return refactoringMap
    }

    private fun createCommitFromRepo(
        githubAPI: GithubAPI,
        ghRepository: GHRepository,
        commitMessage: String,
        modifiedCus: List<CompilationUnit>,
        baseTreeSha: String,
        parentCommitSha: String,
        newBranchName: String
    ) {
        // Step 1: Create blobs for the file contents
        val treeBuilder = ghRepository.createTree().baseTree(baseTreeSha)

        modifiedCus.forEach { cu ->
            val content = cu.toString()
            val path = cu.storage.map { projectRoot.relativize(it.path) }.orElse(null)
                ?: throw RuntimeException("Failed to get path for CompilationUnit")

            treeBuilder.add(path.toString().replace("\\", "/"), content, false)
        }
        println("Added ${modifiedCus.size} blobs to tree")

        val tree: GHTree
        try {
            tree = treeBuilder.create()
        } catch (e: IOException) {
            throw RuntimeException("Failed to create tree", e)
        }
        println("Created tree: ${tree.sha}")


        // Step 3: Create the commit
        val commit = runBlocking {
            githubAPI.createCommit(
                ghRepository.ownerName,
                ghRepository.name,
                commitMessage,
                tree.sha,
                parentCommitSha,
            )
        }

        println("Created commit: ${commit.sha}")
        // Update the branch to point to the new commit
        try {
            val ref = ghRepository.getRef("heads/$newBranchName")
            println("Updating branch reference to ${commit.sha}")
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
