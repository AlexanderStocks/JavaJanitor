package Github.APIFormats

data class Branch(val name: String, val commit: Commit, val protected: Boolean)
