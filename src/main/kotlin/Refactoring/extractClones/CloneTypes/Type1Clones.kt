package Refactoring.extractClones.CloneTypes

import Refactoring.extractClones.NormalizedCodeFragment
import spoon.reflect.code.CtBlock

class Type1Clones {

    fun find(normalizedCodeFragments: List<NormalizedCodeFragment>): List<Pair<NormalizedCodeFragment, NormalizedCodeFragment>> {
        val clonePairs = mutableListOf<Pair<NormalizedCodeFragment, NormalizedCodeFragment>>()
        val n = normalizedCodeFragments.size
        val checkedStatements = mutableSetOf<NormalizedCodeFragment>()

        outer@ for (i in 0 until n) {
            if (checkedStatements.contains(normalizedCodeFragments[i])) {
                continue@outer
            }

            for (j in i + 1 until n) {
                val fragment1 = normalizedCodeFragments[i]
                val fragment2 = normalizedCodeFragments[j]

                if (fragment1.normalizedCode == fragment2.normalizedCode) {
                    // Check if the parent blocks are identical
                    val parentBlock1 = fragment1.statement.parent
                    val parentBlock2 = fragment2.statement.parent

                    if (parentBlock1 == parentBlock2) {
                        // Skip the rest of the elements within the block
                        if (parentBlock1 is CtBlock<*>) {
                            checkedStatements.addAll(normalizedCodeFragments.filter { it.statement.parent == parentBlock1 })
                        }
                        continue@outer
                    }

                    clonePairs.add(Pair(fragment1, fragment2))
                }
            }
        }

        return clonePairs
    }
}