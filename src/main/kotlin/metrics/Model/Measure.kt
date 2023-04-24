package metrics.Model

import kotlinx.serialization.Serializable

@Serializable
data class Measure(val metric: Metric, val value: Double)