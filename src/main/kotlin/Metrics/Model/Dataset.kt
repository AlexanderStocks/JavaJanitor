package Metrics.Model
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

object Dataset {
    private const val DATASET_PATH = "./dataset"

    private val dataset = HashMap<String, Suite>()

//    fun store(classQualifiedName: String, measure: Measure) {
//        val suite: Suite? = if (dataset.containsKey(classQualifiedName)) {
//            dataset[classQualifiedName]
//        } else {
//            Suite(classQualifiedName).also { dataset[classQualifiedName] = it }
//        }
//        println("- Class Name: $classQualifiedName [${measure.metric.shortName} = ${measure.value}]")
//        suite?.addMeasure(measure)
//    }

    fun store(classQualifiedName: String, measure: Measure) {
        val suite: Suite = dataset[classQualifiedName] ?: Suite(classQualifiedName).also { dataset[classQualifiedName] = it }
        println("- Class Name: $classQualifiedName [${measure.metric.shortName} = ${measure.value}]")
        suite.addMeasure(measure)
    }

    fun list() = dataset.values

    fun clear() {
        dataset.clear()
    }

//    fun toCSV(): String {
//        val buffer = StringBuffer("CLASS_QNAME")
//        for (suite in list()) {
//            for (measure in suite.measures) {
//                buffer.append(";")
//                buffer.append(measure.metric.shortName)
//            }
//            break
//        }
//
//        for (suite in list()) {
//            buffer.append("\n")
//            buffer.append(suite.toCSV())
//        }
//        return buffer.toString()
//    }

    fun toCSV(): String {
        return buildString {
            append("CLASS_QNAME")
            list().firstOrNull()?.measures?.joinToString(separator = ";") { measure ->
                ";${measure.metric.shortName}"
            }?.let { append(it) }
            list().forEach { suite ->
                append("\n")
                append(suite.toCSV())
            }
        }
    }

//    fun generateCSVFile(datasetName: String?) {
//        val directory = File(DATASET_PATH)
//        if (!directory.exists()) {
//            directory.mkdir()
//        }
//
//        val calendar = Calendar.getInstance()
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
//        dateFormat.timeZone = calendar.timeZone
//        val reportName = StringBuffer()
//
//        if (datasetName == null || datasetName.isEmpty().not()) {
//            reportName.append(datasetName)
//            reportName.append("-")
//        }
//
//        reportName.append("dataset-")
//        reportName.append(dateFormat.format(calendar.time))
//        reportName.append(".csv")
//
//        val fullReportName = reportName.toString()
//
//        val reportFile = File(directory, fullReportName)
//        try {
//            FileWriter(reportFile, true).use { fileWriter ->
//                PrintWriter(fileWriter).use { printWriter ->
//                    printWriter.append(toCSV())
//                    printWriter.close()
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        dataset.clear()
//    }

    fun generateCSVFile(datasetName: String?) {
        val directory = File(DATASET_PATH)
        if (!directory.exists()) {
            directory.mkdir()
        }

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").apply { timeZone = calendar.timeZone }
        val reportName = StringBuilder()

        if (datasetName == null || datasetName.isEmpty().not()) {
            reportName.append(datasetName)
            reportName.append("-")
        }
        reportName.apply {
            append("dataset-")
            append(dateFormat.format(calendar.time))
            append(".csv")
        }

        val fullReportName = reportName.toString()

        val reportFile = File(directory, fullReportName)
        try {
            FileWriter(reportFile, true).use { fileWriter ->
                PrintWriter(fileWriter).use { printWriter ->
                    printWriter.append(toCSV())
                    printWriter.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        dataset.clear()
    }
}