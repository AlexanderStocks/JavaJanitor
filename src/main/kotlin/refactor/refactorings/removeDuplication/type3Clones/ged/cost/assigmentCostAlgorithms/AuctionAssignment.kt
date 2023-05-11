package refactor.refactorings.removeDuplication.type3Clones.ged.cost.assigmentCostAlgorithms

import kotlin.math.max

object AuctionAssignment {
    fun execute(costMatrix: Array<IntArray>): Int {
        val n = costMatrix.size
        val m = costMatrix[0].size
        var maxVal = Int.MIN_VALUE
        for (row in costMatrix) {
            for (value in row) {
                if (value > maxVal) {
                    maxVal = value
                }
            }
        }

        val epsilon = max(1, maxVal / n)

        val prices = IntArray(m)
        val assignment = IntArray(n) { -1 }
        val unassigned = mutableListOf<Int>()

        for (i in 0 until n) {
            unassigned.add(i)
        }

        while (unassigned.isNotEmpty()) {
            val bidder = unassigned.removeFirst()
            var maxObjValue = Int.MIN_VALUE
            var maxObjIdx = -1
            var secondMaxObjValue = Int.MIN_VALUE

            for (j in 0 until m) {
                val objValue = costMatrix[bidder][j] - prices[j]
                if (objValue > maxObjValue) {
                    secondMaxObjValue = maxObjValue
                    maxObjValue = objValue
                    maxObjIdx = j
                } else if (objValue > secondMaxObjValue) {
                    secondMaxObjValue = objValue
                }
            }

            val increment = maxObjValue - secondMaxObjValue + epsilon

            if (assignment.contains(maxObjIdx)) {
                val previousBidder = assignment.indexOf(maxObjIdx)
                unassigned.add(previousBidder)
            }

            assignment[bidder] = maxObjIdx
            prices[maxObjIdx] += increment
        }

        return assignment.mapIndexed { index, value -> costMatrix[index][value] }.sum()
    }
}
