package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.common.cloneFinders.BaseCloneFinder

class Type2CloneFinder : BaseCloneFinder() {
    private val elementReplacers = listOf(Type2CloneElementReplacer::replace)

    override fun find(methods: List<MethodDeclaration>): List<List<MethodDeclaration>> {
        return findClones(methods.map { ProcessedMethod(it, elementReplacers) }) { methodsWithSameMetrics ->
            methodsWithSameMetrics.groupBy { method ->
                Type2CloneElementReplacer.replace(method.normalisedMethod).body
            }.values.toList()
        }.map { it.map { method -> method.method } }
    }
}