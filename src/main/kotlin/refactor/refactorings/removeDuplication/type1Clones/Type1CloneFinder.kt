package refactor.refactorings.removeDuplication.type1Clones

import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.common.cloneFinders.BaseCloneFinder

class Type1CloneFinder : BaseCloneFinder() {
    override fun find(
        methods: List<MethodDeclaration>
    ): List<List<MethodDeclaration>> {
        return findClones(methods.map { ProcessedMethod(it) }) { methodGroupsByMetrics ->
            methodGroupsByMetrics.filter { it.normalisedMethod.body.isPresent && it.normalisedMethod.body.get().statements.size > 0 }
                .groupBy { method ->
                    method.normalisedMethod.body
                }.values.toList()
        }.map { it.map { method -> method.method } }
    }
}