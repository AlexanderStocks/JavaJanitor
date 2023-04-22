package Github.APIFormats.push

import Github.APIFormats.Installation
import Github.APIFormats.Repository

data class PushEvent(
    val ref: String,
    val before: String,
    val after: String,
    val repository: Repository,
    val installation: Installation,
    val pusher: Pusher,
    val sender: Sender
)
