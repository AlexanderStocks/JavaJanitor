package builder.runners

import builder.BuildRunner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

class GradleBuildRunner(private val projectLocation: String) : BuildRunner {
    override fun buildProject(): Boolean {
        var isSuccessful = false

        try {
            // First, do a 'gradlew clean install'
            var builder = ProcessBuilder("gradlew.bat", "clean" , "install")
            builder.directory(File(projectLocation))
            var process = builder.start()

            val installReader = BufferedReader(InputStreamReader(process.inputStream))

            installReader.useLines { lines ->
                lines.forEach { line ->
                    println(line)
                }
            }

            process.waitFor()

            // Then, do a 'gradlew build'

            builder = ProcessBuilder("gradlew.bat", "build")
            builder.directory(File(projectLocation))
            process = builder.start()

            val buildReader = BufferedReader(InputStreamReader(process.inputStream))

            buildReader.useLines { lines ->
                lines.forEach { line ->
                    if (line.contains("BUILD SUCCESSFUL")) {
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
