import Refactorings.RemoveEmptyElseStatementsProcessor
import org.eclipse.jdt.core.compiler.InvalidInputException
import spoon.Launcher
import spoon.reflect.code.CtComment
import spoon.reflect.declaration.CtClass
import spoon.reflect.visitor.filter.TypeFilter
import java.io.File
import java.io.FileWriter

class RefactoringService(private val git: Git) {
    private val launcher = Launcher()

    init {
        launcher.addInputResource(git.projectName)
        launcher.factory.environment.setCommentEnabled(true)
        launcher.buildModel()
    }

    fun refactor() {
        addStructureToClasses()
        launcher.model.processWith(RemoveEmptyElseStatementsProcessor())
        replaceModifiedFiles()
    }

    private fun addStructureToClasses() {
        val classes = launcher.model.getElements(TypeFilter(CtClass::class.java))
        classes.forEach {
            it.addComment<CtComment>(
                it.factory.Code()
                    .createComment(
                        "ProjectStructure: ${File(it.position.file.path).relativeTo(File(git.projectName).absoluteFile).path}",
                        CtComment.CommentType.INLINE
                    )
            )
        }
    }

    private fun replaceModifiedFiles() {
        launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach { ctClass ->
            val comment = getAndRemoveComment(ctClass)
            val originalFile = ctClass.position.file.toString()
            val updatedFile = ctClass.prettyprint()

            if (isModified(originalFile, updatedFile)) {
                val fileToReplace =
                    File("${git.projectName}/${comment.content.substring("ProjectStructure:".length).trim()}")
                val fileWriter = FileWriter(fileToReplace)

                fileWriter.write(updatedFile)
                fileWriter.close()
            }
        }
    }

    private fun getStructureComment(ctClass: CtClass<*>): CtComment {
        val comments = ctClass.comments.filter { it.content.startsWith("ProjectStructure:") }
        if (comments.size != 1) {
            throw InvalidInputException("File ${ctClass.simpleName} has multiple ProjectStructure comments.")
        }
        return comments[0]
    }

    private fun getAndRemoveComment(ctClass: CtClass<*>): CtComment {
        val comment = getStructureComment(ctClass)
        ctClass.removeComment<CtComment>(comment)

        return comment
    }

    private fun isModified(str1: String, str2: String): Boolean {
        val lines1 = str1.lines().filterNot { it.trim().startsWith("//") }
        val lines2 = str2.lines().filterNot { it.trim().startsWith("//") }
        return lines1.map { it.trim() } != lines2.map { it.trim() }
    }
}