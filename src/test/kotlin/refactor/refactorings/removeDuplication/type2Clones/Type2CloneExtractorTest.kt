package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class Type2CloneExtractorTest {

    @TempDir
    lateinit var tempDir: Path

    private fun createCompilationUnitWithClones(): CompilationUnit {
        val code = """
            class Test {
                void method1() {
                    int x = 1 + 2;
                    System.out.println(x);
                }

                void method2() {
                    int y = 1 + 2;
                    System.out.println(y);
                }

                void method3() {
                    int z = 2 * 3;
                    System.out.println(z);
                }
            }
        """.trimIndent()

        // Create a temporary file and write the contents to it.
        val tempFile = tempDir.resolve("Test.java")
        Files.write(tempFile, code.toByteArray())

        // Read and parse the temporary file.

        return StaticJavaParser.parse(tempFile)
    }

    @Test
    fun `process compilation units with type 2 clones`() {
        val cu = createCompilationUnitWithClones()
        val type2CloneExtractor = Type2CloneExtractor()

        val cus = listOf(cu)
        val modifiedFiles = type2CloneExtractor.process(cus)

        assertEquals(1, modifiedFiles.size)
        val modifiedCu = cus.first()

        val methods = modifiedCu.findAll(MethodDeclaration::class.java)
        assertEquals(4, methods.size)

        val genericMethod = methods.firstOrNull { it.nameAsString.endsWith("Generic") }
        assertNotNull(genericMethod)
        assertEquals("method1Generic", genericMethod!!.nameAsString)

        val method1 = methods.firstOrNull { it.nameAsString == "method1" }
        val method2 = methods.firstOrNull { it.nameAsString == "method2" }
        val method3 = methods.firstOrNull { it.nameAsString == "method3" }

        assertNotNull(method1)
        assertNotNull(method2)
        assertNotNull(method3)

        assertTrue(method1!!.body.isPresent)
        assertTrue(method2!!.body.isPresent)
        assertTrue(method3!!.body.isPresent)

        val method1Call = method1.body.get().statements.firstOrNull { it.isExpressionStmt }
        val method2Call = method2.body.get().statements.firstOrNull { it.isExpressionStmt }

        assertNotNull(method1Call)
        assertNotNull(method2Call)

        assertEquals("method1Generic();", method1Call!!.toString().trim())
        assertEquals("method1Generic();", method2Call!!.toString().trim())
    }
}
