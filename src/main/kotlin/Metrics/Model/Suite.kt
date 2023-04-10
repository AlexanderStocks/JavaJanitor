package Metrics.Model

import kotlinx.serialization.Serializable

@Serializable
class Suite(var qualifiedName: String, private var _location: String) {

    private val metrics = mutableMapOf<Metric, Double>()


    fun addMeasure(measure: Measure) {
        metrics[measure.metric] = measure.value
    }

    val size: Int
        get() = metrics.size

    fun getMetric(metric: Metric): Double = metrics.getOrDefault(metric, 0.0)

    fun keys(): Set<Metric> = metrics.keys

    val location: String
        get() = _location
}