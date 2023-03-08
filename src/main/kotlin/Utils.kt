import Github.APIFormats.InitialiseEvent
import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.nio.file.Paths
import java.security.interfaces.RSAPrivateKey
import java.util.*
import java.util.zip.ZipFile
import kotlin.system.exitProcess

class Utils {
    companion object {
        fun loadPrivateKey(privateKeyPath: String): Algorithm {
            val pemParser = PEMParser(FileReader(privateKeyPath))
            val converter = JcaPEMKeyConverter().setProvider("BC")
            val readObject = pemParser.readObject() as PEMKeyPair
            val privateKey = converter.getPrivateKey(readObject.privateKeyInfo)
            return Algorithm.RSA256(null, privateKey as RSAPrivateKey)
        }

        fun parseWebhookPayload(body: String): InitialiseEvent {
            return try {
                Gson().fromJson(body, InitialiseEvent::class.java)
            } catch (e: Exception) {
                println("Failed to parse webhook: ${e.message}")
                exitProcess(1)
            }
        }

        fun createGitHubClient(installationAccessToken: String): GitHub {
            return GitHubBuilder()
                .withJwtToken(installationAccessToken)
                .build()
        }

        fun extractZipFile(zipFile: File, outputDir: String) {
            val zip = ZipFile(zipFile)
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryPath = entry.name.replace('\\', '/')
                if (entry.isDirectory) {
                    File(outputDir, entryPath).mkdirs()
                } else {
                    val entryIn = zip.getInputStream(entry)
                    val entryOut = BufferedOutputStream(FileOutputStream(File(outputDir, entryPath)))
                    entryIn.copyTo(entryOut)
                    entryIn.close()
                    entryOut.close()
                }
            }
            zip.close()
            zipFile.deleteRecursively()
        }

        fun javaFileToBase64(file: File): String {
            return Base64.getEncoder().encodeToString(file.readBytes())
        }

        fun getRelativePathToParentDirectory(file: String, parentDir: String): String {
            val filePath = Paths.get(file)
            val parentPath = Paths.get(parentDir)
            return parentPath.relativize(filePath).toString()
        }
    }
}