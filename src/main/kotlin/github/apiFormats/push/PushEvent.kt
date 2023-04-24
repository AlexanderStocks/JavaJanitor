package github.apiFormats.push

import github.apiFormats.Installation
import github.apiFormats.Repository

data class PushEvent(
    val ref: String,
    val before: String,
    val after: String,
    val repository: Repository,
    val installation: Installation,
    val pusher: Pusher,
    val sender: Sender
)
