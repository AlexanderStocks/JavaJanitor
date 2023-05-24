package refactor.refactorings.recursionToIteration

import com.github.javaparser.ast.CompilationUnit
import refactor.Refactoring
import java.nio.file.Path

class RecursionToIterationProcessor : Refactoring{

    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        return cus.mapNotNull { convertRecursionToIterationAndSave(it) }
    }

    private fun convertRecursionToIterationAndSave(cu: CompilationUnit): CompilationUnit? {
        val originalCuString = cu.toString()
        RecursionConverter().convertRecursionToIteration(cu)

        return if (originalCuString != cu.toString()) cu else null
    }
}