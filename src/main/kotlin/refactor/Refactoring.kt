package refactor

import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path

interface Refactoring {
    fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit>
}