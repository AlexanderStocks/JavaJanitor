package github.apiFormats

data class InstallationRepositoriesEvent(
    val action: String,
    val installation: Installation,
    val repositories_added: List<Repository>,
    val sender: User
)
