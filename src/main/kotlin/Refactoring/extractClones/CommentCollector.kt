package Refactoring.extractClones

import spoon.reflect.code.CtBlock
import spoon.reflect.code.CtComment
import spoon.reflect.visitor.CtScanner

class CommentCollector : CtScanner() {

    private val comments = mutableListOf<CtComment>()
    private var inMethod = false

    override fun <R : Any?> visitCtBlock(block: CtBlock<R>?) {
        val toChange = !inMethod

        inMethod = true

        super.visitCtBlock(block)


        inMethod = !toChange
    }

    override fun visitCtComment(comment: CtComment?) {
        super.visitCtComment(comment)
        if (inMethod) {
            comments.add(comment!!)
        }
    }

    fun getComments() = comments
}