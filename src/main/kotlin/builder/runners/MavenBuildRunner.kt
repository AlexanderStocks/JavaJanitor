package builder.runners

import builder.BuildRunner
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MavenBuildRunner(private val projectLocation: String) : BuildRunner {
    override fun buildProject(): Boolean {
        var isSuccessful = false
        println(projectLocation)
        try {
            val mavenHome = "C:\\Users\\Stock\\Downloads\\apache-maven-3.9.2-bin\\apache-maven-3.9.2" // Specify the path to your Maven installation

            val command = "cmd /c ${mavenHome}\\bin\\mvn.cmd verify"
            val process = Runtime.getRuntime().exec(command, null, File(projectLocation))

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("BUILD SUCCESS")) {
                        isSuccessful = true
                    }
                }
            }

            process.waitFor()
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return false
        }

        println("is successful: $isSuccessful")
        return isSuccessful
    }
}
