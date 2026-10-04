package net.onefivefour.echolist.feature.note.domain.model

internal data class Note(
    val id: String,
    val parentDir: String,
    val title: String,
    val content: String,
    val updatedAt: Long // Unix timestamp in milliseconds
)
