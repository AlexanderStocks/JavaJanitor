package refactor.refactorings.removeDuplication.type3ClonesOld

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type3CloneExtractor : CloneExtractor() {
    private val similarityThreshold = 0.8

    private val type3CloneFinder = Type3CloneFinder(similarityThreshold)
    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return type3CloneFinder.find(processedMethods)
    }
}