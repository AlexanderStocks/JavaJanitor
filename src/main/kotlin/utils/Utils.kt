package utils

import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import github.apiFormats.InitialiseEvent
import github.apiFormats.Repository
import github.apiFormats.push.PushEvent
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.security.interfaces.RSAPrivateKey
import java.util.*
import java.util.zip.ZipFile
import kotlin.system.exitProcess

object Utils {
    fun loadPrivateKey(privateKeyPath: String): Algorithm {
        val pemParser = PEMParser(FileReader(privateKeyPath))
        val converter = JcaPEMKeyConverter().setProvider("BC")
        val readObject = pemParser.readObject() as PEMKeyPair
        val privateKey = converter.getPrivateKey(readObject.privateKeyInfo)
        return Algorithm.RSA256(null, privateKey as RSAPrivateKey)
    }

    fun parseWebhookPayload(body: String, eventType: String): Any? {
        return when (eventType) {
            "installation" -> {
                try {
                    Gson().fromJson(body, InitialiseEvent::class.java)
                } catch (e: Exception) {
                    println("Failed to parse installation webhook: ${e.message}")
                    exitProcess(1)
                }
            }

            "push" -> {
                try {
                    Gson().fromJson(body, PushEvent::class.java)
                } catch (e: Exception) {
                    println("Failed to parse push webhook: ${e.message}")
                    exitProcess(1)
                }
            }

            else -> {
                println("Unsupported event type: $eventType")
                null
            }
        }
    }

    fun createGitHubClient(installationAccessToken: String): GitHub {
        return GitHubBuilder().withJwtToken(installationAccessToken).build()
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

    fun getReposWithIds(payload: Any): List<Pair<Repository, Int>> = when (payload) {
        is InitialiseEvent -> payload.repositories.map { it to payload.installation.id }
        is PushEvent -> listOf(payload.repository to payload.installation.id)
        else -> {
            println("Unsupported payload type")
            emptyList()
        }
    }
}