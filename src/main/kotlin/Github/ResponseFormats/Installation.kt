package Github.ResponseFormats

data class Installation(
    val id: Int,
    val account: User,
    val repository_selection: String,
    val access_tokens_url: String,
    val repositories_url: String,
    val html_url: String,
    val app_id: Int,
    val app_slug: String,
    val target_id: Int,
    val target_type: String,
    val permissions: Map<String, String>,
    val events: List<String>,
    val created_at: String,
    val updated_at: String,
    val single_file_name: String?,
    val has_multiple_single_files: Boolean,
    val single_file_paths: List<String>,
    val suspended_by: String?,
    val suspended_at: String?
)