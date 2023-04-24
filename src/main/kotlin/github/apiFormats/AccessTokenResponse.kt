package github.apiFormats

data class AccessTokenResponse(
    val token: String,
    val expires_at: String,
    val permissions: Map<String, String>,
    val repository_selection: String
)