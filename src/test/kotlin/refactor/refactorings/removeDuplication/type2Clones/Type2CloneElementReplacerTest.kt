package refactor.refactorings.removeDuplication.type2Clones

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Type2CloneElementReplacerTest {

    @Test
    fun replaceElementsInMethod() {
        val code = """
            class Test {
                void method1() {
                    int x = 1;
                    int y = x + 2;
                    System.out.println(y);
                }
            }
        """.trimIndent()

        val cu = StaticJavaParser.parse(code)
        val method = cu.findFirst(MethodDeclaration::class.java).get()

        Type2CloneElementReplacer.replace(method)

        val modifiedCode = method.toString()
        assertTrue(modifiedCode.contains("var1"))
        assertTrue(modifiedCode.contains("var2"))
    }
}

