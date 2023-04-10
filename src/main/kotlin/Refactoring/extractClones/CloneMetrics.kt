package Refactoring.extractClones


import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfArguments
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfAssignmentStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfAssignmentsThroughFunctionCalls
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfConditionalStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfFunctionCalls
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfInputStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfIterationStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfLines
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfLocalVariables
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfOutputStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfReturnStatements
import Metrics.Methods.MethodMetrics.Companion.calculateNumberOfSelectionStatements
import Metrics.Model.Measure
import Metrics.Model.MethodDataset
import Metrics.Model.Metric
import repoPath
import spoon.reflect.declaration.CtMethod
import java.io.File

class CloneMetrics(private val methodDataset: MethodDataset) {

    fun process(methods: MutableSet<CtMethod<*>>) {

        val metricFunctions = listOf<Pair<Metric, (CtMethod<*>) -> Double>>(
            Metric.LINES to ::calculateNumberOfLines,
            Metric.ARGUMENTS to ::calculateNumberOfArguments,
            Metric.LOCAL_VARIABLES to ::calculateNumberOfLocalVariables,
            Metric.FUNCTION_CALLS to ::calculateNumberOfFunctionCalls,
            Metric.CONDITIONAL_STATEMENTS to ::calculateNumberOfConditionalStatements,
            Metric.ITERATION_STATEMENTS to ::calculateNumberOfIterationStatements,
            Metric.RETURN_STATEMENTS to ::calculateNumberOfReturnStatements,
            Metric.INPUT_STATEMENTS to ::calculateNumberOfInputStatements,
            Metric.OUTPUT_STATEMENTS to ::calculateNumberOfOutputStatements,
            Metric.FUNCTION_ASSIGNMENTS to ::calculateNumberOfAssignmentsThroughFunctionCalls,
            Metric.SELECTION_STATEMENTS to ::calculateNumberOfSelectionStatements,
            Metric.ASSIGNMENT_STATEMENTS to ::calculateNumberOfAssignmentStatements
        )

        methods.forEach { method ->
            val qualifiedName = method.declaringType.qualifiedName
            val methodName = method.simpleName
            val methodQualifiedName = "$qualifiedName.$methodName"

            val methodLocation = File(method.position.file.path).relativeTo(File(repoPath).absoluteFile).path

            metricFunctions.forEach { (metric, function) ->
                val value = function.invoke(method)
                storeMetric(methodQualifiedName, methodLocation, metric, value)
            }
        }

        methodDataset.generateJSONFiles("methodDataset")
    }

    private fun storeMetric(
        methodQualifiedName: String,
        methodLocation: String,
        metric: Metric,
        value: Double
    ) {
        methodDataset.store(methodQualifiedName, methodLocation, Measure(metric, value), true)
    }

}