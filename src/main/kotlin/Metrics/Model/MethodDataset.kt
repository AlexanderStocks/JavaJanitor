package Metrics.Model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class MethodDataset {
    private val DATASET_PATH = "./src/main/resources/dataset/methods"

    private val dataset = HashMap<String, Suite>()

    fun store(entityQualifiedName: String, methodLocation: String, measure: Measure, isMethod: Boolean = false) {
        val suite: Suite =
            dataset[entityQualifiedName] ?: Suite(entityQualifiedName, methodLocation).also {
                dataset[entityQualifiedName] = it
            }
        val entityType = if (isMethod) "Method" else "Class"
        println("- $entityType Name: $entityQualifiedName [${measure.metric.shortName} = ${measure.value}]")
        suite.addMeasure(measure)
    }

    fun list() = dataset.values

    fun clear() {
        dataset.clear()
    }

    private fun toJSON(suite: Suite): String {
        return Json.encodeToString(suite)
    }

    fun generateJSONFiles(datasetName: String?) {
        val directory = File(DATASET_PATH)
        if (!directory.exists()) {
            directory.mkdir()
        }
        
        dataset.forEach { (methodQualifiedName, suite) ->
            val reportName = StringBuilder()

            if (datasetName == null || datasetName.isEmpty().not()) {
                reportName.append(datasetName)
                reportName.append("-")
            }

            reportName.apply {
                append(suite.location.replace("\\", "_").removeSuffix(".java"))
                append("-")
                append(methodQualifiedName.replace(".", "_"))
                append(".json")
            }

            val fullReportName = reportName.toString()
            val reportFile = File(directory, fullReportName)
            try {
                FileWriter(reportFile, true).use { fileWriter ->
                    PrintWriter(fileWriter).use { printWriter ->
                        printWriter.append(toJSON(suite))
                        printWriter.close()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        dataset.clear()
    }
}