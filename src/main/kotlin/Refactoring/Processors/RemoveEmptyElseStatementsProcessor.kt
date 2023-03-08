package Refactoring.Processors

import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtIf
import spoon.reflect.code.CtStatement

class RemoveEmptyElseStatementsProcessor : AbstractProcessor<CtIf>() {
    override fun process(element: CtIf?) {
        element ?: return
        element.getElseStatement<CtStatement>()?.let { elseStatement ->
            if (!elseStatement.isImplicit && elseStatement.directChildren.isEmpty()) {
                element.setElseStatement<CtIf>(null)
            }
        }
    }
}