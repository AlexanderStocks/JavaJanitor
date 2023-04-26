package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration

class ASTTreeBuilder {
    fun buildTree(compilationUnit: CompilationUnit): Tree {
        val method = compilationUnit.findFirst(MethodDeclaration::class.java).get()
        val root = buildTreeNode(method)
        return Tree(root)
    }

    private fun buildTreeNode(node: Node): TreeNode {
        val children = node.childNodes.map { buildTreeNode(it) }.toMutableList()
        return TreeNode(node.javaClass.simpleName, children)
    }
}
