import Refactoring.extractClones.ExtractClones
import spoon.Launcher

fun main() {
    val launcher = Launcher()
    launcher.factory.environment.setCommentEnabled(true)
    launcher.addInputResource("src/main/resources/TestCases/Type2Clones.java")
    launcher.buildModel()
    launcher.model.processWith(ExtractClones())
}