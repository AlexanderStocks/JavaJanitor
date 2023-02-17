package Metrics.Model

class Suite(private var classQualifiedName: String) {
    val measures: List<Measure> = mutableListOf()

    fun addMeasure(measure: Measure) {
        if (measures.stream().filter { m -> m.metric.shortName == measure.metric.shortName }.count() == 0L) {
            measures.plus(measure)
        }
    }

    fun toCSV(): String {
        val buffer = StringBuilder(classQualifiedName)

        for (measure in measures) {
            buffer.append(";")
            buffer.append(measure.value)
        }
        return buffer.toString()
    }
}