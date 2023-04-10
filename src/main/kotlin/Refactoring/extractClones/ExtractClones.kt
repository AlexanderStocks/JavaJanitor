package Refactoring.extractClones

import Metrics.Model.MethodDataset
import Refactoring.extractClones.CloneTypes.Type1Clones
import Refactoring.extractClones.CloneTypes.Type2Clones
import spoon.processing.AbstractProcessor
import spoon.reflect.declaration.CtClass

class ExtractClones : AbstractProcessor<CtClass<*>>() {

    private val codeNormalizer = CodeNormalizer()
    private val type1Clones = Type1Clones()

    override fun process(element: CtClass<*>) {
        val methodDataset = MethodDataset()
        val type2Clones = Type2Clones(methodDataset)
        type2Clones.process(element.methods)

    }

    private fun printPairs(pairs: List<Pair<NormalizedCodeFragment, NormalizedCodeFragment>>, type: String) {
        println("$type found:")
        pairs.forEach { (fragment1, fragment2) ->
            println("Clone Pair:\n$fragment1\n---\n$fragment2\n")
        }
    }


}