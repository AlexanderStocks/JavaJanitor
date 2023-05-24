import kotlinx.coroutines.runBlocking
import refactor.refactorings.removeDuplication.common.methodCreation.OpenAiClient

    private val openAiClient = OpenAiClient()
    suspend fun getMethodName(methodBody: String): String {
        return try {
            openAiClient.askGptForMethodNames(methodBody).firstOrNull() ?: "genericMethod"
        } catch (e : Exception) {
            println("Error during refactoring and testing: ${e.message}")
            e.printStackTrace()
            "genericMethod"
        }
    }
    fun main () {
        val methodBody = """
                System.out.println("Hello, world!");
        """.trimIndent()

        val methodName = runBlocking { getMethodName(methodBody)}
        println(methodName)
    }

