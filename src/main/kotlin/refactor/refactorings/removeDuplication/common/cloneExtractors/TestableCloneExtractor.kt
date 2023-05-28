package refactor.refactorings.removeDuplication.common.cloneExtractors

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.methodCreation.MethodCreator
import tester.TestRunner
import java.nio.file.Path
import java.time.Instant
import java.util.*
import kotlin.time.Duration

class TestableCloneExtractor : CloneExtractor() {

    override fun applyRefactoringAndTest(
        cu: CompilationUnit,
        cloneGroup: List<MethodDeclaration>,
        projectRoot: Path
    ): Optional<MethodDeclaration> {
        val result: Optional<MethodDeclaration>
        val timeNow = Instant.now()

            val clone = cu.clone()
            val clonedCloneGroup = clone.findAll(MethodDeclaration::class.java)
                .filter { cloneGroup.map { method -> method.nameAsString }.contains(it.nameAsString) }
            result = try {
                val testRunner = TestRunner.create(projectRoot.toString())
                val initialTestResults = testRunner.runTests()

                if (initialTestResults.isEmpty()) {
                    return Optional.empty()
                }

                val createdMethod = super.applyRefactoringAndTest(clone, clonedCloneGroup, projectRoot)
                clone.storage.get().save()

                if (createdMethod.isPresent) {
                    val testResults = testRunner.runTests()
                    println(testResults)

                    if (initialTestResults.isEmpty() || initialTestResults != testResults) {
                        cu.storage.get().save()
                        val endTime = Instant.now()
                        val processingTime = endTime.toEpochMilli() - timeNow.toEpochMilli()
                        println("Failed processing time: $processingTime ms")
                        return Optional.empty()
                    }
                }

                createdMethod
            } catch (e: Exception) {
                Optional.empty()
            }

            cu.storage.get().save()

            if (result.isPresent) {
                copyRefactoringChanges(cu, clonedCloneGroup, result.get())
            }

        val endTime = Instant.now()
        val processingTime = endTime.toEpochMilli() - timeNow.toEpochMilli()
        println("Succesfull processing time: $processingTime ms")



        return result
    }


    private fun copyRefactoringChanges(
        cu: CompilationUnit,
        cloneGroup: List<MethodDeclaration>,
        newMethod: MethodDeclaration
    ) {
        val originalMethods = cu.findAll(MethodDeclaration::class.java)
        cloneGroup.forEach { clone ->
            println(originalMethods.first { it.nameAsString == clone.nameAsString && it.parameters == clone.parameters }.replace(clone))
        }

        val first = cloneGroup.first()

        val targetClassOrInterface = first.findAncestor(ClassOrInterfaceDeclaration::class.java).orElse(null)
        val targetEnum = first.findAncestor(EnumDeclaration::class.java).orElse(null)

        val target = targetClassOrInterface ?: targetEnum

        val cuTargetClass = targetClassOrInterface?.findAll(ClassOrInterfaceDeclaration::class.java)?.first { it.nameAsString == target.nameAsString }
        val cuTargetEnum = targetEnum?.findAll(EnumDeclaration::class.java)?.first { it.nameAsString == target.nameAsString }

        when {
            cuTargetClass != null -> cuTargetClass.addMember(newMethod)
            cuTargetEnum != null -> cuTargetEnum.addMember(newMethod)
        }
    }

}