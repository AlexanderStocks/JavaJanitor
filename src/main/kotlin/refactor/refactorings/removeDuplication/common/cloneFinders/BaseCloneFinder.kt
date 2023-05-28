package refactor.refactorings.removeDuplication.common.cloneFinders

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import refactor.refactorings.removeDuplication.common.ProcessedMethod

abstract class BaseCloneFinder : CloneFinder {
    protected fun findClones(
        methods: List<ProcessedMethod>,
        groupByCloneCriteria: (List<ProcessedMethod>) -> List<List<ProcessedMethod>>
    ): List<List<ProcessedMethod>> {

        val methodsByClassOrInterfaceOrEnum = methods.groupBy { processedMethod ->
            val method = processedMethod.method
            val targetClassOrInterface = method.findAncestor(ClassOrInterfaceDeclaration::class.java).orElse(null)
            val targetEnum = method.findAncestor(EnumDeclaration::class.java).orElse(null)

            when {
                targetClassOrInterface != null -> targetClassOrInterface
                targetEnum != null -> targetEnum
                else -> throw RuntimeException("Method is not in a class, interface, or enum: ${method.signature}")
            }
        }

        val groupsWithSameNormalizedBody = mutableListOf<List<ProcessedMethod>>()

        methodsByClassOrInterfaceOrEnum.values.forEach { methodsInSameClass ->
            val groupedByMetrics = methodsInSameClass.groupBy { it.metrics }
            groupedByMetrics.values.forEach { methodsWithSameMetrics ->
                val groupedByCloneCriteria = groupByCloneCriteria(methodsWithSameMetrics)
                val methodsWithSameNormalizedBody = groupedByCloneCriteria
                    .filter { it.size > 1 }
                groupsWithSameNormalizedBody.addAll(methodsWithSameNormalizedBody)
            }
        }

        return groupsWithSameNormalizedBody
    }
}

