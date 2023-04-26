package refactor.refactorings.removeDuplication.type3Clones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type3CloneFinder {

    fun find(methodsAndMetrics: List<ProcessedMethod>): List<List<MethodDeclaration>> {
        val methodTrees = methodsAndMetrics.map { methodAndMetric ->
            Pair(buildTreeFromMethod(methodAndMetric.method), methodAndMetric)
        }

        val clusters = mutableListOf<MutableList<MethodDeclaration>>()

        for (i in methodTrees.indices) {
            for (j in i + 1 until methodTrees.size) {
                val tree1 = methodTrees[i].first
                val tree2 = methodTrees[j].first

                val distance = Tree.treeEditDistance(tree1, tree2)

                // Customize the threshold for determining clones
                val similarityThreshold = 0.8
                val similarity = 1.0 - (distance.toDouble() / (tree1.size() + tree2.size()))

                if (similarity >= similarityThreshold) {
                    val method1 = methodTrees[i].second.method
                    val method2 = methodTrees[j].second.method

                    // Merge clusters if methods are already in different clusters
                    val cluster1 = clusters.find { methods -> methods.contains(method1) }
                    val cluster2 = clusters.find { methods -> methods.contains(method2) }

                    if (cluster1 != null && cluster2 != null && cluster1 != cluster2) {
                        cluster1.addAll(cluster2)
                        clusters.remove(cluster2)
                    } else if (cluster1 != null) {
                        cluster1.add(method2)
                    } else if (cluster2 != null) {
                        cluster2.add(method1)
                    } else {
                        clusters.add(mutableListOf(method1, method2))
                    }
                }
            }
        }

        return clusters
    }

    private fun buildTreeFromMethod(method: MethodDeclaration): Tree {
        val cu = StaticJavaParser.parse("class Dummy { $method }")
        val treeBuilder = ASTTreeBuilder()
        return treeBuilder.buildTree(cu)
    }
}
