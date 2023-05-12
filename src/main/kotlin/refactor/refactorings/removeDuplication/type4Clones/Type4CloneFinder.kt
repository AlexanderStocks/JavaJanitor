package refactor.refactorings.removeDuplication.type4Clones

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import refactor.refactorings.removeDuplication.common.ProcessedMethod
import refactor.refactorings.removeDuplication.type3Clones.Type3CloneFinder

class Type4CloneFinder(similarityThreshold: Double) : Type3CloneFinder(similarityThreshold)
