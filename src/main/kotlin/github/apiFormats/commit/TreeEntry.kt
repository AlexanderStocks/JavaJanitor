package github.apiFormats.commit

data class TreeEntry(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String?,
    val size: Int?,
    val url: String?
)
