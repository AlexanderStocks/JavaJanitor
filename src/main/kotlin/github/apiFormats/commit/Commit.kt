package github.apiFormats.commit

data class Commit(
    val sha: String,
    val node_id: String,
    val url: String,
    val html_url: String,
    val parents: List<Parent>
)