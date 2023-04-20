package refactor.refactorings.removeDuplication.type1Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type1CloneExtractor : CloneExtractor() {
    private val type2CloneFinder = Type1CloneFinder()

    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return type2CloneFinder.find(processedMethods)
    }
}