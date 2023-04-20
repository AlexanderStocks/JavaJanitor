package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type3CloneExtractor : CloneExtractor() {
    private val type3CloneFinder = Type3CloneFinder()
    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return type3CloneFinder.find(processedMethods)
    }
}