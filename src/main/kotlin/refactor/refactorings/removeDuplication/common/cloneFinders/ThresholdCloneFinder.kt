package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.body.MethodDeclaration
import metric.Model.Metric
import refactor.refactorings.removeDuplication.common.ProcessedMethod

abstract class ThresholdCloneFinder(private val threshold: Double) : BaseCloneFinder() {
    protected fun groupMethodsByRange(methods: List<ProcessedMethod>): List<List<ProcessedMethod>> {
        val groupedMethods = mutableListOf<MutableList<ProcessedMethod>>()

        for (currentMethod in methods) {
            var addedToGroup = false

            for (group in groupedMethods) {
                if (isSimilarToGroup(currentMethod, group)) {
                    group.add(currentMethod)
                    addedToGroup = true
                    break
                }
            }

            if (!addedToGroup) {
                groupedMethods.add(mutableListOf(currentMethod))
            }
        }

        return groupedMethods
    }

    private fun isSimilarToGroup(method: ProcessedMethod, group: List<ProcessedMethod>): Boolean {
        val groupAverageMetrics = calculateAverageMetrics(group)

        for ((metricKey, metricValue) in method.metrics) {
            val groupMetricAverage = groupAverageMetrics[metricKey] ?: continue
            val range = metricValue * 100 / groupMetricAverage

            if (range < threshold) {
                return false
            }
        }

        return true
    }

    private fun calculateAverageMetrics(group: List<ProcessedMethod>): Map<Metric, Double> {
        val sumMetrics = mutableMapOf<Metric, Double>()

        for (method in group) {
            for ((metricKey, metricValue) in method.metrics) {
                sumMetrics[metricKey] = sumMetrics.getOrDefault(metricKey, 0.0) + metricValue
            }
        }

        return sumMetrics.mapValues { (_, sum) -> sum / group.size }
    }
}
