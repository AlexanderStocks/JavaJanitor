package refactor.refactorings.removeDuplication.type3ClonesPDG.ged.cost.assigmentCostAlgorithms


object HungarianAlgorithm {
    fun execute(costMatrix: Array<IntArray>): Int {
        val n = costMatrix.size
        val labels = IntArray(n * 2)
        val xy = IntArray(n) { -1 }
        val yx = IntArray(n) { -1 }
        val slack = IntArray(n)
        val slackx = IntArray(n)
        val prev = IntArray(n)

        for (i in 0 until n) {
            labels[i] = costMatrix[i].maxOrNull() ?: 0
        }

        for (u in 0 until n) {
            slack.fill(Int.MAX_VALUE)
            val visitedX = BooleanArray(n)
            val visitedY = BooleanArray(n)

            var x = u
            var y = -1

            while (x != -1) {
                visitedX[x] = true
                var minSlack = Int.MAX_VALUE
                var minSlackX = -1

                for (j in 0 until n) {
                    if (!visitedY[j]) {
                        val curSlack = labels[x] + labels[n + j] - costMatrix[x][j]

                        if (curSlack < slack[j]) {
                            slack[j] = curSlack
                            slackx[j] = x
                        }

                        if (slack[j] < minSlack) {
                            minSlack = slack[j]
                            minSlackX = j
                            prev[j] = x  // Update the prev array here
                        }
                    }
                }

                for (j in 0 until n) {
                    if (visitedY[j]) {
                        labels[n + j] += minSlack
                        labels[yx[j]] -= minSlack
                    } else {
                        slack[j] -= minSlack
                    }
                }

                y = minSlackX
                x = slackx[y]

                while (x != -1) {
                    val nextY = xy[x]
                    yx[y] = x
                    xy[x] = y
                    y = nextY
                    if (y != -1) {
                        x = prev[y]
                    } else {
                        break
                    }
                }

            }
        }

        return xy.mapIndexed { index, value -> costMatrix[index][value] }.sum()
    }
}
