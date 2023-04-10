import Refactoring.extractClones.ExtractClones
import spoon.Launcher

fun main() {
    val launcher = Launcher()
    launcher.environment.noClasspath = true
    launcher.addInputResource("src/main/resources/TestCases")
    launcher.addProcessor(ExtractClones())
    launcher.run()
}