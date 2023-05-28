package builder

import builder.runners.GradleBuildRunner
import builder.runners.MavenBuildRunner
import java.nio.file.Files
import java.nio.file.Path

interface BuildRunner {
    fun buildProject(): Boolean

    companion object {
        fun create(projectLocation: String): BuildRunner {
            val projectPath = Path.of(projectLocation)

            return when {
                Files.exists(projectPath.resolve("build.gradle")) -> GradleBuildRunner(projectLocation)
                Files.exists(projectPath.resolve("pom.xml")) -> MavenBuildRunner(projectLocation)
                else -> throw IllegalArgumentException("Unknown build system in $projectLocation")
            }
        }
    }
}