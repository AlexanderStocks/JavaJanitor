package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.body.MethodDeclaration

interface CloneFinder {
    fun find(methods: List<MethodDeclaration>): List<List<MethodDeclaration>>
}
