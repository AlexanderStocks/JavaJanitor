package refactor.refactorings.removeDuplication.common

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MethodCreatorTest {

    @Test
    fun createGenericMethodForClones() {
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
            }
        """.trimIndent()

        val cu = StaticJavaParser.parse(code)
        val methods = cu.findAll(MethodDeclaration::class.java)

        val cloneGroup = listOf(methods[0], methods[1])
        val clones = listOf(cloneGroup)

        val methodCreator = MethodCreator(cu, clones)
        methodCreator.create()

        val genericMethod = cu.findFirst(MethodDeclaration::class.java) { it.nameAsString == "fooGeneric" }.get()
        assertEquals("fooGeneric", genericMethod.nameAsString)
    }
}
