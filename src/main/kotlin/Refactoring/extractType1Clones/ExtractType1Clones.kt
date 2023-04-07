package Refactoring.extractType1Clones

import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass

class ExtractType1Clones : AbstractProcessor<CtClass<*>>() {

    private val codeNormalizer = CodeNormalizer()
    private val codeRefactorer = CodeRefactorer()

    override fun process(element: CtClass<*>) {

        element.methods.forEach { method ->
            val codeFragments = codeNormalizer.normalizeMethods(method)
            val clonePairs = findType1CodeClonesInMethods(codeFragments)

            if (clonePairs.isNotEmpty()) {
                println("Type 1 code clones found in method ${method.signature}:")
                clonePairs.forEach { (fragment1, fragment2) ->
                    println("Clone Pair:\n$fragment1\n---\n$fragment2\n")
                }
                codeRefactorer.extractRepeatedBehaviorIntoMethod(element, clonePairs)
            } else {
                println("No Type 1 code clones found in method ${method.signature}.")
            }
        }
    }

    private fun findType1CodeClonesInMethods(normalizedCodeFragments: List<NormalizedCodeFragment>): List<Pair<NormalizedCodeFragment, NormalizedCodeFragment>> {
        val clonePairs = mutableListOf<Pair<NormalizedCodeFragment, NormalizedCodeFragment>>()
        val n = normalizedCodeFragments.size

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val fragment1 = normalizedCodeFragments[i]
                val fragment2 = normalizedCodeFragments[j]

                if (fragment1.normalizedCode == fragment2.normalizedCode) {
                    clonePairs.add(Pair(fragment1, fragment2))
                }
            }
        }

        return clonePairs
    }
}