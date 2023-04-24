package refactor.refactorings.removeDuplication.type1Clones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type1CloneFinderTest {

    @Test
    fun findType1Clones() {
        val code = """
            class Test {
                void foo() {
                    int x = 1;
                    int y = 2;
                    int z = x + y;
                }

                void bar() {
                    int x = 1;
                    int y = 2;
                    int z = x + y;
                }

                void baz() {
                    int x = 3;
                    int y = 4;
                    int z = x + y;
                }
            }
        """.trimIndent()

        val cu = StaticJavaParser.parse(code)
        val methods = cu.findAll(MethodDeclaration::class.java)
        val processedMethods = methods.map { ProcessedMethod(it) }

        val cloneFinder = Type1CloneFinder()
        val clones = cloneFinder.find(processedMethods)

        assertEquals(1, clones.size)
        assertEquals(2, clones[0].size)
        assertEquals("foo", clones[0][0].nameAsString)
        assertEquals("bar", clones[0][1].nameAsString)
    }
}