package refactor

import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path

interface Refactoring {
    fun process(cus: List<CompilationUnit>): List<Path>
}