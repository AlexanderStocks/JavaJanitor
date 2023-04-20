package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type2CloneExtractor : CloneExtractor() {
    private val type2CloneFinder = Type2CloneFinder()

    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return type2CloneFinder.find(processedMethods)
    }
}