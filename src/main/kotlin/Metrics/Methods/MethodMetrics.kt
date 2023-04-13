package Metrics.Methods

import spoon.reflect.code.*
import spoon.reflect.declaration.CtMethod
import spoon.reflect.reference.CtExecutableReference
import spoon.reflect.visitor.filter.TypeFilter

class MethodMetrics {
    companion object {
        fun calculateNumberOfLines(method: CtMethod<*>): Double {
            return method.body?.statements?.size?.toDouble() ?: 0.0
        }

        fun calculateNumberOfArguments(method: CtMethod<*>): Double {
            return method.parameters.size.toDouble()
        }

        fun calculateNumberOfLocalVariables(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtStatement::class.java))
                .filterIsInstance<CtAssignment<*, *>>()
                .count()
                .toDouble()
        }

        fun calculateNumberOfFunctionCalls(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtStatement::class.java))
                .filterIsInstance<CtInvocation<*>>()
                .count()
                .toDouble()
        }

        fun calculateNumberOfConditionalStatements(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtStatement::class.java))
                .filterIsInstance<CtConditional<*>>()
                .count()
                .toDouble()
        }

        fun calculateNumberOfIterationStatements(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtStatement::class.java))
                .filterIsInstance<CtLoop>()
                .count()
                .toDouble()
        }

        fun calculateNumberOfReturnStatements(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtStatement::class.java))
                .filterIsInstance<CtReturn<*>>()
                .count()
                .toDouble()
        }

        fun calculateNumberOfInputStatements(method: CtMethod<*>): Double {
            val inputMethods = setOf(
                "java.util.Scanner.next",
                "java.util.Scanner.nextLine",
                "java.util.Scanner.hasNext",
                "java.util.Scanner.hasNextLine",
                "java.util.Scanner.hasNextInt",
                "java.util.Scanner.hasNextDouble",
                "java.util.Scanner.hasNextBoolean",
                "java.util.Scanner.hasNextByte",
                "java.util.Scanner.hasNextFloat",
                "java.util.Scanner.hasNextLong",
                "java.util.Scanner.hasNextShort",
                "java.io.BufferedReader.readLine",
                "java.io.Console.readLine",
                "java.io.Console.readPassword"
            )

            return method.body.getElements(TypeFilter(CtInvocation::class.java))
                .map { it.executable }
                .count { executable: CtExecutableReference<*> -> executable.simpleName in inputMethods }
                .toDouble()
        }

        fun calculateNumberOfOutputStatements(method: CtMethod<*>): Double {
            val outputMethods = setOf(
                "java.io.PrintStream.println",
                "java.io.PrintStream.print",
                "java.io.PrintStream.printf",
                "java.io.PrintWriter.println",
                "java.io.PrintWriter.print",
                "java.io.PrintWriter.printf",
                "java.io.BufferedWriter.write",
                "java.io.FileWriter.write",
                "java.io.Writer.write",
                "java.io.BufferedWriter.newLine",
                "java.io.FileWriter.newLine",
                "java.io.Writer.newLine"
            )

            return method.body.getElements(TypeFilter(CtInvocation::class.java))
                .map { it.executable }
                .count { executable: CtExecutableReference<*> -> executable.simpleName in outputMethods }
                .toDouble()
        }


        fun calculateNumberOfAssignmentsThroughFunctionCalls(method: CtMethod<*>): Double {
            val functionCallAssignments = mutableSetOf<String>()

            // Find all assignments in the method
            val assignments = method.body.getElements(TypeFilter(CtAssignment::class.java))

            // Check each assignment for a function call on the right-hand side
            assignments.forEach { assignment ->
                assignment.assignment.getElements(TypeFilter(CtInvocation::class.java))
                    .map { it.executable }
                    .filterIsInstance<CtExecutableReference<*>>()
                    .forEach { _ ->
                        val assignmentTarget = assignment.assigned.toString()
                        functionCallAssignments.add(assignmentTarget)
                    }
            }

            return functionCallAssignments.size.toDouble()
        }

        fun calculateNumberOfSelectionStatements(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtIf::class.java)).count().toDouble() +
                    method.body.getElements(TypeFilter(CtSwitch::class.java)).flatMap { switch: CtSwitch<*> ->
                        switch.cases.filterIsInstance<CtCase<*>>()
                    }.count().toDouble()
        }

        fun calculateNumberOfAssignmentStatements(method: CtMethod<*>): Double {
            return method.body.getElements(TypeFilter(CtAssignment::class.java)).size.toDouble() +
                    method.body.getElements(TypeFilter(CtUnaryOperator::class.java))
                        .filter { it.kind == UnaryOperatorKind.POSTINC || it.kind == UnaryOperatorKind.POSTDEC }
                        .size.toDouble()
        }

    }
}
