package refactor.refactorings.removeDuplication.common.equalityTypes

import com.github.javaparser.ast.stmt.BlockStmt
import java.util.*

data class MethodKey(
    val parameters: Set<ParameterKey>,
    val body: Optional<BlockStmt>
)
