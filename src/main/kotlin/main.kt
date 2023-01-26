import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.code.CtComment
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter
import java.io.File

//val commit = git.getLastCommit()
//val bytes = git.readFileFromCommit(commit,"src/Test.java")
//val temp = File.createTempFile("temp", "java")

fun main() {
    val git = Git("https://github.com/AlexanderStocks/Test-IncreaseCyclomaticComplexityByCommit")

    val launcher = Launcher()
    launcher.addInputResource(git.projectName)
    launcher.buildModel()

    val classes = launcher.model.getElements(TypeFilter<CtClass<Any>>(CtClass::class.java))
    classes.forEach {
        it.addComment<CtComment>(
            it.factory.Code()
                .createComment(
                    "ProjectStructure: ${File(it.position.file.path).relativeTo(File(git.projectName).absoluteFile).path}",
                    CtComment.CommentType.INLINE
                )
        )
        print(it.comments)
    }
    //println("Lines of real code = ${LinesOfCode().calculate(launcher.model)}")
    //launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach { println(LinesOfCode().calculate(it)) }
    launcher.prettyprint()

    //launcher.model.processWith(RemoveEmptyElseStatementsProcessor())
    //launcher.environment.setShouldCompile(false)
    //launcher.setSourceOutputDirectory(git.projectName)
    //launcher.modelBuilder.generateProcessedSourceFiles(OutputType.)

    //val spoonOutputDirectory = File("spooned")

//    spoonOutputDirectory.walk().forEach { file ->
//        if (file.isFile) {
//            val packageName =
//                file.toURI().toString().replace(spoonOutputDirectory.toURI().toString(), "").replace("/", ".")
//                    .removeSuffix(".java")
//            val packageDir = packageName.replace(".", "/")
//            val originalFile = File("${git.projectName}/$packageDir/${file.name}")
//            if (originalFile.exists()) {
//                originalFile.delete()
//            }
//            file.renameTo(originalFile)
//        }
//    }
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

fun numClasses(model: CtModel) {
    println("Modules = ${model.allModules}")
    println("Packages = ${model.allPackages}")
    println("Types = ${model.allTypes}")
}
