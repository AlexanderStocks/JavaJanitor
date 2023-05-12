package refactor.refactorings.removeDuplication.type3Clones.ged.cost.assigmentCostAlgorithms

import kotlin.math.max

object AuctionAssignment {
    fun execute(costMatrix: Array<IntArray>): Int {
        val n = costMatrix.size
        val m = costMatrix[0].size
        var minVal = Int.MAX_VALUE
        for (row in costMatrix) {
            for (value in row) {
                if (value < minVal) {
                    minVal = value
                }
            }
        }

        val epsilon = max(1, minVal / n)

        val prices = IntArray(m)
        val assignment = IntArray(n) { -1 }
        val unassigned = mutableListOf<Int>()

        for (i in 0 until n) {
            unassigned.add(i)
        }

        while (unassigned.isNotEmpty()) {
            val bidder = unassigned.removeFirst()
            var minObjValue = Int.MAX_VALUE
            var minObjIdx = -1
            var secondMinObjValue = Int.MAX_VALUE

            for (j in 0 until m) {
                val objValue = costMatrix[bidder][j] + prices[j]
                if (objValue < minObjValue) {
                    secondMinObjValue = minObjValue
                    minObjValue = objValue
                    minObjIdx = j
                } else if (objValue < secondMinObjValue) {
                    secondMinObjValue = objValue
                }
            }

            val decrement = secondMinObjValue - minObjValue + epsilon

            if (assignment.contains(minObjIdx)) {
                val previousBidder = assignment.indexOf(minObjIdx)
                unassigned.add(previousBidder)
            }

            assignment[bidder] = minObjIdx
            prices[minObjIdx] -= decrement
        }

        return assignment.mapIndexed { index, value -> costMatrix[index][value] }.sum()
    }
}
