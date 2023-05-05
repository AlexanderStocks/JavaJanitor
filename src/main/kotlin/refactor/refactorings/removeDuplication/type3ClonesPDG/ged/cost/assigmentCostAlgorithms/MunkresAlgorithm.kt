package refactor.refactorings.removeDuplication.type3ClonesPDG.ged.cost.assigmentCostAlgorithms

object MunkresAlgorithm {
    fun execute(costMatrix: Array<IntArray>): Int {
        val n = costMatrix.size
        val m = costMatrix[0].size

        // Prepare the matrix by subtracting the minimum value from each row
        for (row in costMatrix) {
            val minValue = row.minOrNull() ?: 0
            for (j in row.indices) {
                row[j] -= minValue
            }
        }

        // Subtract the minimum value from each column
        for (j in 0 until m) {
            var minValue = Int.MAX_VALUE
            for (i in 0 until n) {
                minValue = minOf(minValue, costMatrix[i][j])
            }
            for (i in 0 until n) {
                costMatrix[i][j] -= minValue
            }
        }

        val starMatrix = Array(n) { BooleanArray(m) }
        val primeMatrix = Array(n) { BooleanArray(m) }
        val coveredColumns = BooleanArray(m)
        val coveredRows = BooleanArray(n)

        // Step 1: Star zeros
        for (i in 0 until n) {
            for (j in 0 until m) {
                if (costMatrix[i][j] == 0 && !coveredRows[i] && !coveredColumns[j]) {
                    starMatrix[i][j] = true
                    coveredRows[i] = true
                    coveredColumns[j] = true
                }
            }
        }

        coveredRows.fill(false)
        coveredColumns.fill(false)

        while (true) {
            // Step 2: Cover columns containing a starred zero
            for (i in 0 until n) {
                for (j in 0 until m) {
                    if (starMatrix[i][j]) {
                        coveredColumns[j] = true
                    }
                }
            }

            // Check for optimality
            if (coveredColumns.count { it } >= n) {
                break
            }

            var primeRow = -1
            var primeColumn = -1
            var step = 3

            while (true) {
                when (step) {
                    3 -> {
                        // Step 3: Prime uncovered zeros
                        prime@ for (i in 0 until n) {
                            if (!coveredRows[i]) {
                                for (j in 0 until m) {
                                    if (!coveredColumns[j] && costMatrix[i][j] == 0) {
                                        primeMatrix[i][j] = true
                                        primeRow = i
                                        primeColumn = j
                                        step = 4
                                        break@prime
                                    }
                                }
                            }
                        }
                        if (step == 3) {
                            step = 6
                        }
                    }

                    4 -> {
                        // Step 4: Check for a starred zero in the primed zero's row
                        var starredRow = -1
                        for (j in 0 until m) {
                            if (starMatrix[primeRow][j]) {
                                starredRow = j
                                break
                            }
                        }

                        if (starredRow == -1) {
                            step = 5
                        } else {
                            primeMatrix[primeRow][primeColumn] = false
                            coveredRows[primeRow] = true
                            coveredColumns[starredRow] = false
                            step = 3
                        }
                    }

                    5 -> {
                        // Step 5: Augmenting path
                        val path = mutableListOf<Pair<Int, Int>>()
                        var currentRow = primeRow
                        var currentColumn = primeColumn

                        path.add(currentRow to currentColumn)

                        while (true) {
                            val starredColumn = (0 until m).firstOrNull { starMatrix[currentRow][it] }
                            if (starredColumn == null) {
                                break
                            } else {
                                path.add(currentRow to starredColumn)
                                currentColumn = starredColumn
                            }

                            val primedRow = (0 until n).firstOrNull { primeMatrix[it][currentColumn] }
                            if (primedRow == null) {
                                break
                            } else {
                                path.add(primedRow to currentColumn)
                                currentRow = primedRow
                            }
                        }

                        // Update the matrices
                        for ((row, column) in path) {
                            starMatrix[row][column] = !starMatrix[row][column]
                            primeMatrix[row][column] = false
                        }

                        // Reset the covered vectors and prime matrix
                        coveredRows.fill(false)
                        coveredColumns.fill(false)
                        primeMatrix.forEach { it.fill(false) }

                        step = 3
                    }

                    6 -> {
                        // Step 6: Update the cost matrix
                        val minUncoveredValue = (0 until n).flatMap { i ->
                            (0 until m).mapNotNull { j ->
                                if (!coveredRows[i] && !coveredColumns[j]) costMatrix[i][j] else null
                            }
                        }.minOrNull() ?: 0

                        for (i in 0 until n) {
                            for (j in 0 until m) {
                                if (coveredRows[i]) {
                                    costMatrix[i][j] += minUncoveredValue
                                }
                                if (!coveredColumns[j]) {
                                    costMatrix[i][j] -= minUncoveredValue
                                }
                            }
                        }

                        step = 4
                    }
                }
            }
        }

        return (0 until n).sumOf { i ->
            (0 until m).sumOf { j ->
                if (starMatrix[i][j]) costMatrix[i][j] else 0
            }
        }
    }
}

