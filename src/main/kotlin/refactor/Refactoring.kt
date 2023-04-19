package refactor

import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path

abstract class Refactoring {
    abstract fun process(cus: List<CompilationUnit>): List<Path>
}