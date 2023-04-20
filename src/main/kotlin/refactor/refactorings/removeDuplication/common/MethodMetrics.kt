package refactor.refactorings.removeDuplication.common

import Metrics.Methods.MethodMetrics
import Metrics.Model.Metric
import com.github.javaparser.ast.body.MethodDeclaration

class MethodMetrics {

    private val metricFunctions = listOf<Pair<Metric, (MethodDeclaration) -> Double>>(
        Metric.LINES to MethodMetrics.Companion::calculateNumberOfLines,
        Metric.ARGUMENTS to MethodMetrics.Companion::calculateNumberOfArguments,
        Metric.LOCAL_VARIABLES to MethodMetrics.Companion::calculateNumberOfLocalVariables,
        Metric.FUNCTION_CALLS to MethodMetrics.Companion::calculateNumberOfFunctionCalls,
        Metric.CONDITIONAL_STATEMENTS to MethodMetrics.Companion::calculateNumberOfConditionalStatements,
        Metric.ITERATION_STATEMENTS to MethodMetrics.Companion::calculateNumberOfIterationStatements,
        Metric.RETURN_STATEMENTS to MethodMetrics.Companion::calculateNumberOfReturnStatements,
        Metric.FUNCTION_ASSIGNMENTS to MethodMetrics.Companion::calculateNumberOfAssignmentsThroughFunctionCalls,
        Metric.SELECTION_STATEMENTS to MethodMetrics.Companion::calculateNumberOfSelectionStatements,
        Metric.ASSIGNMENT_STATEMENTS to MethodMetrics.Companion::calculateNumberOfAssignmentStatements
    )

    fun process(method: MethodDeclaration): Map<Metric, Double> {
        return metricFunctions.associate { (metric, metricFunction) ->
            metric to metricFunction(method)
        }
    }
}