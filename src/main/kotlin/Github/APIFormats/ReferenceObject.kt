package Github.APIFormats

import kotlinx.serialization.Serializable

@Serializable
data class ReferenceObject(val sha: String, val type: String, val url: String)
