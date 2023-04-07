import Refactoring.extractType1Clones.ExtractType1Clones
import spoon.Launcher

fun main() {
    val launcher = Launcher()
    launcher.environment.noClasspath = true
    launcher.addInputResource("src/main/resources/test.java")
    launcher.addProcessor(ExtractType1Clones())
    launcher.run()

    launcher.prettyprint()
}