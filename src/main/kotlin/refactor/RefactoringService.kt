//package refactoring
//
//import org.eclipse.jdt.core.compiler.InvalidInputException
//import refactoring.refactorings.ExtractType1Clones.ExtractType1Clones
//import spoon.Launcher
//import spoon.reflect.code.CtComment
//import spoon.reflect.declaration.CtClass
//import spoon.reflect.visitor.filter.TypeFilter
//import java.io.File
//import java.io.FileWriter
//
//class RefactoringService(private val repoName: String) {
//    private val launcher = Launcher()
//
//    init {
//        launcher.addInputResource(repoName)
//        launcher.factory.environment.setCommentEnabled(true)
//        launcher.buildModel()
//    }
//
//    fun refactor(): List<File> {
//        addStructureToClasses()
//        launcher.model.processWith(ExtractType1Clones())
//        return replaceModifiedFiles()
//    }
//
//    private fun addStructureToClasses() {
//        launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach {
//            it.addComment<CtComment>(
//                it.factory.Code()
//                    .createComment(
//                        "ProjectStructure: ${File(it.position.file.path).relativeTo(File(repoName).absoluteFile).path}",
//                        CtComment.CommentType.INLINE
//                    )
//            )
//        }
//    }
//
//    private fun replaceModifiedFiles(): List<File> {
//        val modifiedFiles = mutableListOf<File>()
//        launcher.model.getElements(TypeFilter(CtClass::class.java)).forEach { ctClass ->
//            val comment = getAndRemoveComment(ctClass)
//            val originalFile = ctClass.position.file.toString()
//            val updatedFile = ctClass.prettyprint()
//
//            if (isModified(originalFile, updatedFile)) {
//                val fileToReplace = File("${repoName}/${comment.content.substring("ProjectStructure:".length).trim()}")
//                val fileWriter = FileWriter(fileToReplace)
//
//                fileWriter.write(updatedFile)
//                fileWriter.close()
//                modifiedFiles.add(fileToReplace)
//            }
//        }
//        return modifiedFiles
//    }
//
//    private fun getStructureComment(ctClass: CtClass<*>): CtComment {
//        val comments = ctClass.comments.filter { it.content.startsWith("ProjectStructure:") }
//        if (comments.size != 1) {
//            throw InvalidInputException("File ${ctClass.simpleName} has multiple ProjectStructure comments.")
//        }
//        return comments[0]
//    }
//
//    private fun getAndRemoveComment(ctClass: CtClass<*>): CtComment {
//        val comment = getStructureComment(ctClass)
//        ctClass.removeComment<CtComment>(comment)
//
//        return comment
//    }
//
//    private fun isModified(str1: String, str2: String): Boolean {
//        val lines1 = str1.lines().filterNot { it.trim().startsWith("//") }
//        val lines2 = str2.lines().filterNot { it.trim().startsWith("//") }
//        return lines1.map { it.trim() } != lines2.map { it.trim() }
//    }
//}