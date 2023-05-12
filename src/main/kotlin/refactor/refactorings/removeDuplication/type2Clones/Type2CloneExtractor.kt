import refactor.refactorings.removeDuplication.common.CloneExtractor
import refactor.refactorings.removeDuplication.type2Clones.Type2CloneFinder

class Type2CloneExtractor : CloneExtractor() {
    override val cloneFinder = Type2CloneFinder()
}