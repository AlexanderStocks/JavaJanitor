package refactor.refactorings.reformat

import com.github.javaparser.JavaToken
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.printer.PrettyPrinter
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration
import com.google.googlejavaformat.java.Formatter
import com.google.googlejavaformat.java.FormatterException
import refactor.Refactoring
import java.nio.file.Path
import java.nio.file.Files
import java.nio.charset.StandardCharsets

class Reformat : Refactoring {
    override fun process(projectRoot: Path, cus: List<CompilationUnit>): List<CompilationUnit> {
        return cus.mapNotNull { reformatAndSave(it) }
    }

    private fun reformatAndSave(cu: CompilationUnit): CompilationUnit? {
        val formatter = Formatter()

        val prettyPrintedString = try {
            formatter.formatSource(cu.toString())
        } catch (e: FormatterException) {
            e.printStackTrace()
            return null
        }

        val originalFilePath = cu.storage.get().path
        val originalFileString = Files.readString(originalFilePath, StandardCharsets.UTF_8)

        if (prettyPrintedString != originalFileString) {
            Files.write(originalFilePath, prettyPrintedString.toByteArray(StandardCharsets.UTF_8))
            return cu
        }
        return null
    }
}
