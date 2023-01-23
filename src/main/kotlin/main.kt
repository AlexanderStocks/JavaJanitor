import Refactorings.RemoveEmptyElseStatementsProcessor
import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter

//val commit = git.getLastCommit()
//val bytes = git.readFileFromCommit(commit,"src/Test.java")
//val temp = File.createTempFile("temp", "java")

fun main() {
    val git = Git("https://github.com/AlexanderStocks/Test-IncreaseCyclomaticComplexityByCommit")

    val launcher = Launcher()
    launcher.addInputResource(git.projectName)
    launcher.buildModel()

    //println("Lines of real code = ${LinesOfCode().calculate(launcher.model)}")
    //launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach { println(LinesOfCode().calculate(it)) }
    println("First print")

    launcher.model.processWith(RemoveEmptyElseStatementsProcessor())


    println("Final print")
    launcher.prettyprint()
    git.removeRepo()
}

//fun removeEmptyElseStatements(element: CtClass<*>) {
//    val ifs = element.getElements(TypeFilter(CtIf::class.java))
//    for (ifStatement in ifs) {
//        val elseStatement = ifStatement.getElseStatement<CtStatement>()
//        if (elseStatement != null && !elseStatement.isImplicit) {
//            println("Modifying! $ifStatement")
//            ifStatement.setElseStatement<CtIf>(null)
//            println("Modified! $ifStatement")
//        }
//    }
//}

fun classes(model: CtModel) {
    val classes = model.getElements(TypeFilter(CtClass::class.java))
    classes.forEach { println(it.simpleName) }
}

fun numClasses(model: CtModel)  {
    println("Modules = ${model.allModules}")
    println("Packages = ${model.allPackages}")
    println("Types = ${model.allTypes}")
}
