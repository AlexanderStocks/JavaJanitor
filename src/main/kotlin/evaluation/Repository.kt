package evaluation

import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val name: String,
    val full_name: String,
    val size: Int,
    val html_url: String // This is the property for the GitHub URL

)