package github.apiFormats.commit

data class Blob(
    val sha: String,
    val node_id: String,
    val size: Int,
    val url: String,
    val content: String,
    val encoding: String
)
