package refactor.refactorings.removeDuplication.type3ClonesPDG


import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class PDGType3CloneExtractor : CloneExtractor() {
    private val similarityThreshold = 0.8

    private val pdgType3CloneFinder = PDGType3CloneFinder(similarityThreshold)
    override fun findClones(processedMethods: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return pdgType3CloneFinder.find(processedMethods)
    }
}
