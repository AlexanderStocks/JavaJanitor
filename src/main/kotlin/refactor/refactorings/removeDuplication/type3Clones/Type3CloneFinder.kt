package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneFinder
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type3CloneFinder : CloneFinder() {
    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { method ->
                Type3CloneElementReplacer.replace(method.normalisedMethod)
                method.normalisedMethod.body
            }
        }
    }
}