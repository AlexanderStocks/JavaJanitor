package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.CloneFinder
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type2CloneFinder : CloneFinder() {

    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { method ->
                Type2CloneElementReplacer.replace(method.normalisedMethod)
                method.normalisedMethod.body
            }
        }
    }
}