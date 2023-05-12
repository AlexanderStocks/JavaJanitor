package refactor.refactorings.removeDuplication.type1Clones

import refactor.refactorings.removeDuplication.common.CloneExtractor

class Type1CloneExtractor : CloneExtractor() {
    override val cloneFinder = Type1CloneFinder()
}
