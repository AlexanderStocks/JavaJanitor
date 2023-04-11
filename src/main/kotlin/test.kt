import Refactoring.extractClones.ExtractClones
import spoon.Launcher

fun main() {
    val launcher = Launcher()
    launcher.factory.environment.setCommentEnabled(true)
    launcher.addInputResource("src/main/resources/TestCases/comment.java")
    launcher.buildModel()
    launcher.model.processWith(ExtractClones())
    println(System.getProperty("java.version"))
}