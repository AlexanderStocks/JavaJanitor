package Metrics.Model

import kotlinx.serialization.Serializable

@Serializable
class Suite(private var qualifiedName: String, private var _location: String) {

    private val metrics = mutableMapOf<Metric, Double>()


    fun addMeasure(measure: Measure) {
        metrics[measure.metric] = measure.value
    }

    val size: Int
        get() = metrics.size

    val location: String
        get() = _location
}