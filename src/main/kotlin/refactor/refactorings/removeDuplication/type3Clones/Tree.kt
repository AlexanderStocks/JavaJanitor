package refactor.refactorings.removeDuplication.type3Clones

class Tree(private val root: TreeNode) {

    companion object {
        fun treeEditDistance(tree1: Tree, tree2: Tree): Int {
            val n = tree1.size()
            val m = tree2.size()
            val dp = Array(n + 1) { IntArray(m + 1) }

            // Initialize the base cases
            for (i in 0..n) {
                dp[i][0] = i
            }

            for (j in 0..m) {
                dp[0][j] = j
            }

            // Compute the edit distance using dynamic programming
            for (i in 1..n) {
                for (j in 1..m) {
                    val node1 = tree1.getPostorderNode(i - 1)
                    val node2 = tree2.getPostorderNode(j - 1)

                    val cost = if (node1.label == node2.label) 0 else 1

                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                }
            }

            return dp[n][m]
        }
    }

    fun size(): Int {
        return getSize(root)
    }

    private fun getSize(node: TreeNode?): Int {
        if (node == null) {
            return 0
        }

        var size = 1
        for (child in node.children) {
            size += getSize(child)
        }
        return size
    }

    fun getPostorderNode(index: Int): TreeNode {
        val postorderList = mutableListOf<TreeNode>()
        traversePostorder(root, postorderList)
        return postorderList[index]
    }

    private fun traversePostorder(node: TreeNode?, postorderList: MutableList<TreeNode>) {
        if (node == null) {
            return
        }

        for (child in node.children) {
            traversePostorder(child, postorderList)
        }
        postorderList.add(node)
    }
}