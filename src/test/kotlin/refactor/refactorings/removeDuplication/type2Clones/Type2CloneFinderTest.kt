package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import refactor.refactorings.removeDuplication.common.ProcessedMethod

class Type2CloneFinderTest {

    @Test
    fun findType2Clones() {
        val code = """
            class Test {
                void method1() {
                    int x = 1;
                    int y = x + 2;
                    System.out.println(y);
                }

                void method2() {
                    int a = 1;
                    int b = a + 2;
                    System.out.println(b);
                }
            }
        """.trimIndent()

        val cu = StaticJavaParser.parse(code)
        val methods = cu.findAll(MethodDeclaration::class.java)
        val processedMethods = methods.map { ProcessedMethod(it) }

        val finder = Type2CloneFinder()
        val clones = finder.find(processedMethods)

        assertEquals(1, clones.size)
        assertEquals(2, clones.first().size)
    }
}