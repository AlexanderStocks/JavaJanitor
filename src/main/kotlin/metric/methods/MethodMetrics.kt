package metric.methods

import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.ConditionalExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver

class MethodMetrics {


    companion object {

        private val symbolSolver = JavaSymbolSolver(ReflectionTypeSolver())

        fun calculateNumberOfLines(method: MethodDeclaration): Double {
            return method.body.map { it.statements.size.toDouble() }.orElse(0.0)
        }

        fun calculateNumberOfArguments(method: MethodDeclaration): Double {
            return method.parameters.size.toDouble()
        }

        fun calculateNumberOfLocalVariables(method: MethodDeclaration): Double {
            return method.findAll(AssignExpr::class.java).count().toDouble()
        }

        fun calculateNumberOfFunctionCalls(method: MethodDeclaration): Double {
            return method.findAll(MethodCallExpr::class.java).count().toDouble()
        }

        fun calculateNumberOfConditionalStatements(method: MethodDeclaration): Double {
            return method.findAll(ConditionalExpr::class.java).count().toDouble()
        }

        fun calculateNumberOfIterationStatements(method: MethodDeclaration): Double {
            return method.findAll(WhileStmt::class.java).count().toDouble() +
                    method.findAll(ForStmt::class.java).count().toDouble() +
                    method.findAll(ForEachStmt::class.java).count().toDouble() +
                    method.findAll(DoStmt::class.java).count().toDouble()
        }

        fun calculateNumberOfReturnStatements(method: MethodDeclaration): Double {
            return method.findAll(ReturnStmt::class.java).count().toDouble()
        }


        fun calculateNumberOfInputStatements(method: MethodDeclaration): Double {
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

            return method.findAll(MethodCallExpr::class.java)
                .count { methodCall ->
                    val resolvedMethod =
                        symbolSolver.resolveDeclaration(methodCall, ResolvedMethodDeclaration::class.java)
                    val declaringType = resolvedMethod.declaringType() as? ResolvedReferenceType
                    val qualifiedName = declaringType?.qualifiedName + "." + resolvedMethod.name
                    qualifiedName in inputMethods
                }
                .toDouble()
        }

        fun calculateNumberOfOutputStatements(method: MethodDeclaration): Double {
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

            return method.findAll(MethodCallExpr::class.java)
                .count { methodCall ->
                    val resolvedMethod =
                        symbolSolver.resolveDeclaration(methodCall, ResolvedMethodDeclaration::class.java)
                    val declaringType = resolvedMethod.declaringType() as? ResolvedReferenceType
                    val qualifiedName = declaringType?.qualifiedName + "." + resolvedMethod.name
                    qualifiedName in outputMethods
                }
                .toDouble()
        }


        fun calculateNumberOfAssignmentsThroughFunctionCalls(method: MethodDeclaration): Double {
            val functionCallAssignments = mutableSetOf<String>()

            val assignments = method.findAll(AssignExpr::class.java)
            assignments.forEach { assignment ->
                assignment.findAll(MethodCallExpr::class.java).forEach { _ ->
                    val assignmentTarget = assignment.target.toString()
                    functionCallAssignments.add(assignmentTarget)
                }
            }

            return functionCallAssignments.size.toDouble()
        }

        fun calculateNumberOfSelectionStatements(method: MethodDeclaration): Double {
            return method.findAll(IfStmt::class.java).count().toDouble() +
                    method.findAll(SwitchStmt::class.java).flatMap { switchStmt ->
                        switchStmt.entries
                    }.count().toDouble()
        }

        fun calculateNumberOfAssignmentStatements(method: MethodDeclaration): Double {
            return method.findAll(AssignExpr::class.java).count().toDouble() +
                    method.findAll(UnaryExpr::class.java).count {
                        it.operator == UnaryExpr.Operator.POSTFIX_INCREMENT ||
                                it.operator == UnaryExpr.Operator.POSTFIX_DECREMENT
                    }.toDouble()
        }
    }
}
