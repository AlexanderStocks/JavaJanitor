package Github

data class InitialiseEvent(
    val action: String,
    val installation: Installation,
    val repositories: List<Repository>,
    val sender: User
)