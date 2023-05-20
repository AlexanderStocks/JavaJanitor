package refactor.refactorings.removeDuplication.type4Clones

import refactor.refactorings.removeDuplication.type2Clones.Type2CloneElementReplacer
import refactor.refactorings.removeDuplication.type3Clones.Type3CloneFinder

class Type4CloneFinder(similarityThreshold: Double) : Type3CloneFinder(similarityThreshold) {
    override val elementReplacers = listOf(Type2CloneElementReplacer::replace, Type4CloneElementReplacer::replace)
}