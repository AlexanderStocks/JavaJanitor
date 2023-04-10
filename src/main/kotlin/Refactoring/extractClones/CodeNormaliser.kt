package Refactoring.extractClones

import spoon.reflect.code.CtStatement
import spoon.reflect.declaration.CtMethod
import spoon.reflect.visitor.filter.TypeFilter

class CodeNormalizer {
    fun normalizeMethods(executable: CtMethod<*>): List<NormalizedCodeFragment> {
        val statements = executable.getElements(TypeFilter(CtStatement::class.java))
        return statements.map { NormalizedCodeFragment(it) }
    }
}

data class NormalizedCodeFragment(val statement: CtStatement) {
    val normalizedCode = normalizeStatement(statement)

    private fun normalizeStatement(statement: CtStatement): String {
        return statement.toString()
            .replace(Regex("\\s+"), " ") // Remove extra whitespace
            .replace(Regex("//.*"), "") // Remove line comments
            .replace(Regex("/\\*.*?\\*/"), "") // Remove block comments
            .trim()
    }
}