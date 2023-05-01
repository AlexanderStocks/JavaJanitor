package refactor.refactorings.removeRedundantTernaryOperators

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.ConditionalExpr
import refactor.Refactoring
import java.nio.file.Path

class RemoveRedundantTernaryOperators : Refactoring {
    override fun process(cus: List<CompilationUnit>): List<Path> {
        return cus.mapNotNull { removeRedundantTernaryOperatorsAndSave(it) }
    }

    private fun removeRedundantTernaryOperatorsAndSave(cu: CompilationUnit): Path? {
        val originalCuString = cu.toString()
        removeRedundantTernaryOperators(cu)

        return if (originalCuString != cu.toString()) cu.storage.get().path else null
    }

    private fun removeRedundantTernaryOperators(cu: CompilationUnit) {
        val conditionalExpressions = cu.findAll(ConditionalExpr::class.java)

        conditionalExpressions.filter { it.thenExpr.toString() == it.elseExpr.toString() }
            .forEach { it.replace(it.thenExpr.clone()) }
    }
}
