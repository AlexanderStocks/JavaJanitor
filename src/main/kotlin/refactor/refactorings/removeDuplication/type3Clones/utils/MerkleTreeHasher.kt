package refactor.refactorings.removeDuplication.type3Clones.utils

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

class MerkleTreeHasher {
    private val hmac = HMac(SHA256Digest())

    init {
        hmac.init(KeyParameter(ByteArray(32))) // Using an all-zero key for simplicity
    }

    fun generateNodeHashCode(node: PDGNode): ByteArray {
        return hashLabel(node)
    }

    fun generateEdgeHashCode(edge: DefaultEdge, graph: Graph<PDGNode, DefaultEdge>): ByteArray {
        val sourceNode = graph.getEdgeSource(edge)
        val targetNode = graph.getEdgeTarget(edge)

        return hashEdge(sourceNode, targetNode)
    }

    private fun hashLabel(node: PDGNode): ByteArray {
        val labelBytes = node.content.toByteArray()
        val output = ByteArray(hmac.macSize)

        hmac.reset()
        hmac.update(labelBytes, 0, labelBytes.size)
        hmac.doFinal(output, 0)

        return output
    }

    private fun hashEdge(sourceNode: PDGNode, targetNode: PDGNode): ByteArray {
        val sourceHash = generateNodeHashCode(sourceNode)
        val targetHash = generateNodeHashCode(targetNode)
        val output = ByteArray(hmac.macSize)

        hmac.reset()
        hmac.update(sourceHash, 0, sourceHash.size)
        hmac.update(targetHash, 0, targetHash.size)
        hmac.doFinal(output, 0)

        return output
    }
}
