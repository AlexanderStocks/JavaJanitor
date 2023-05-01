package refactor.refactorings.removeEmptyElse

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.stmt.IfStmt
import com.github.javaparser.ast.stmt.Statement
import refactor.Refactoring
import java.nio.file.Path

class RemoveEmptyElse : Refactoring {
    override fun process(cus: List<CompilationUnit>): List<Path> {
        return cus.mapNotNull { removeEmptyElseBlocksAndSave(it) }
    }

    private fun removeEmptyElseBlocksAndSave(cu: CompilationUnit): Path? {
        val originalCuString = cu.toString()
        removeEmptyElseBlocks(cu)
        return if (originalCuString != cu.toString()) cu.storage.get().path else null
    }

    private fun removeEmptyElseBlocks(cu: CompilationUnit) {
        cu.findAll(IfStmt::class.java).forEach { ifStmt ->
            ifStmt.elseStmt.ifPresentOrElse({ elseStmt : Statement ->
                if (elseStmt.isBlockStmt && elseStmt.asBlockStmt().statements.isEmpty()) {
                    ifStmt.removeElseStmt()
                }
            }, { /* Do nothing if there's no else statement */ })
        }
    }
}
