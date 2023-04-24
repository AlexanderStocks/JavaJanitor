package github.apiFormats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class Reference(
    val ref: String,
    val node_id: String,
    val url: String, @SerialName("object") val refObject: ReferenceObject
)
