package Metrics.Model

import java.util.*

class Suite(private var classQualifiedName: String) {
    val measures: Collection<Measure> = Vector()

    fun getClassQualifiedName(): String {
        return classQualifiedName
    }

    fun setClassQualifiedName(classQualifiedName: String) {
        this.classQualifiedName = classQualifiedName
    }


    fun addMeasure(measure: Measure) {
        if (measures.stream().filter { m -> m.metric.shortName == measure.metric.shortName }.count() == 0L) {
            measures.plus(measure)
        }
    }

    fun toCSV(): String {
        val buffer = StringBuffer(classQualifiedName)

        for (measure in measures) {
            buffer.append(";")
            buffer.append(measure.value)
        }
        return buffer.toString()
    }

}