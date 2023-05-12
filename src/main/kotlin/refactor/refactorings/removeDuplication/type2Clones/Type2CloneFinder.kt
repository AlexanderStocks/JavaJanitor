package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.common.cloneFinders.BaseCloneFinder

class Type2CloneFinder : BaseCloneFinder() {
    override fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        return findClones(methodsAndMetrics) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { method ->
                Type2CloneElementReplacer.replace(method.normalisedMethod).body
            }.values.toList()
        }.map { methods -> methods.map { method -> method.method } }
    }
}