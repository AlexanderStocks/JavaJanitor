package refactor.refactorings.removeDuplication.common

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MethodNormaliserTest {

    @Test
    fun normaliseMethodHasNoComments() {
        val code = """
            class Test {
                // This is a comment
                void foo() {
                    int x = 1; // Another comment
                    int y = 2;
                    int z = x + y;
                }
            }
        """.trimIndent()

        val cu = StaticJavaParser.parse(code)
        val method = cu.findFirst(MethodDeclaration::class.java).get()

        val methodNormaliser = MethodNormaliser()
        val normalisedMethod = methodNormaliser.normalise(method)

        assertEquals(0, normalisedMethod.allContainedComments.size)
    }
}
