package refactor.refactorings.removeDuplication.type4Clones

import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer
import refactor.refactorings.removeDuplication.type3Clones.Type3CloneFinder

class Type4CloneExtractor : CloneExtractor() {
    private val threshold = 0.7
    override val cloneFinder = Type3CloneFinder(threshold)
    override val elementReplacers = listOf(Type2CloneElementReplacer::replace, Type4CloneElementReplacer::replace)
    override val requiresTesting = true
}