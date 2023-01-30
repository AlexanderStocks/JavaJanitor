import Refactorings.RemoveEmptyElseStatementsProcessor
import org.eclipse.jdt.core.compiler.InvalidInputException
import spoon.Launcher
import spoon.reflect.CtModel
import spoon.reflect.code.CtComment
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter
import java.io.File
import java.io.FileWriter

//val commit = git.getLastCommit()
//val bytes = git.readFileFromCommit(commit,"src/Test.java")
//val temp = File.createTempFile("temp", "java")

fun main() {
    val git = Git("https://github.com/AlexanderStocks/Test-IncreaseCyclomaticComplexityByCommit")

    val launcher = Launcher()
    launcher.addInputResource(git.projectName)
    launcher.factory.environment.setCommentEnabled(true)
    launcher.buildModel()

    addStructureToClasses(git.projectName, launcher)
    //////////////////////////////////////////Do Refactoring//////////////////////////////////////////////////
    // println("Lines of real code = ${LinesOfCode().calculate(launcher.model)}")
    // launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach { println(LinesOfCode().calculate(it)) }

    launcher.model.processWith(RemoveEmptyElseStatementsProcessor())


    /////////////////////////////////////////Write changes to repo////////////////////////////////////

    try {
        replaceModifiedFiles(git.projectName, launcher)
    } catch (e: InvalidInputException) {
        println("Replacing modified files failed: ${e.message}")
    }


    //git.removeRepo()

}

fun isModified(str1: String, str2: String): Boolean {
    val lines1 = str1.lines().filterNot { it.trim().startsWith("//") }
    val lines2 = str2.lines().filterNot { it.trim().startsWith("//") }
    return lines1.map { it.trim() } != lines2.map { it.trim() }
}

fun getStructureComment(ctClass: CtClass<*>): CtComment {
    val comments = ctClass.comments.filter { it.content.startsWith("ProjectStructure:") }
    if (comments.size != 1) {
        throw InvalidInputException("File ${ctClass.simpleName} has multiple ProjectStructure comments.")
    }
    return comments[0]
}

fun getAndRemoveComment(ctClass: CtClass<*>): CtComment {
    val comment = getStructureComment(ctClass)
    ctClass.removeComment<CtComment>(comment)

    return comment
}

fun replaceModifiedFiles(projectName: String, launcher: Launcher) {
    launcher.model.getElements(TypeFilter(CtClass::class.java))
        .forEach { ctClass ->
            val comment = getAndRemoveComment(ctClass)
            val updatedFile = ctClass.prettyprint()
            val originalFile = ctClass.position.file.toString()

            if (!updatedFile.equals(originalFile)) {
                val fileToReplace = File("$projectName/${comment.content.substring("ProjectStructure:".length).trim()}")
                val fileWriter = FileWriter(fileToReplace)

                fileWriter.write(updatedFile)
                fileWriter.close()
            }
        }
}


fun addStructureToClasses(projectName: String, launcher: Launcher) {
    val classes = launcher.model.getElements(TypeFilter(CtClass::class.java))
    classes.forEach {
        it.addComment<CtComment>(
            it.factory.Code()
                .createComment(
                    "ProjectStructure: ${File(it.position.file.path).relativeTo(File(projectName).absoluteFile).path}",
                    CtComment.CommentType.INLINE
                )
        )
    }
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
