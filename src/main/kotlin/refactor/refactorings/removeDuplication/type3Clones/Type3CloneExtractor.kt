package refactor.refactorings.removeDuplication.type3Clones

import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer

open class Type3CloneExtractor : CloneExtractor() {
    private val threshold = 0.7
    override val cloneFinder = Type3CloneFinder(threshold)
    override val elementReplacers = listOf(Type2CloneElementReplacer::replace)
    override val requiresTesting: Boolean = true
}