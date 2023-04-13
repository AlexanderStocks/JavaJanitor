package Refactoring.extractClones.MethodProcessors


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
import Metrics.Model.Metric
import spoon.reflect.declaration.CtMethod

class MethodMetrics {
    private val metricFunctions = listOf<Pair<Metric, (CtMethod<*>) -> Double>>(
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

    fun process(method: CtMethod<*>): Map<Metric, Double> {
        return metricFunctions.associate { (metric, metricFunction) ->
            metric to metricFunction(method)
        }
    }
}