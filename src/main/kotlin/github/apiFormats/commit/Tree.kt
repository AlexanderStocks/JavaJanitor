package github.apiFormats.commit

data class Tree(
    val sha: String,
    val url: String,
    val tree: List<TreeEntry>
)
