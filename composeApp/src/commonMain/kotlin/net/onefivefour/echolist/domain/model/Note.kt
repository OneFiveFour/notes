package net.onefivefour.echolist.domain.model

data class Note(
    val id: String,
    val parentDir: String,
    val title: String,
    val content: String,
    val updatedAt: Long // Unix timestamp in milliseconds
)