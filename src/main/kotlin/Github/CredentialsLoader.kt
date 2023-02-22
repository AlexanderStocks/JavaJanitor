package Github

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class CredentialsLoader (private val filename: String) {
    fun load(): Map<String, Any> = this::class.java.classLoader.getResourceAsStream(filename)
        .use { Yaml().load(it) as Map<String, Any> }
}