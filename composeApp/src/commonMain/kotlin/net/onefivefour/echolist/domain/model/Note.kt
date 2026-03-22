package net.onefivefour.echolist.domain.model

data class Note(
    val filePath: String,
    val title: String,
    val content: String,
    val updatedAt: Long // Unix timestamp in milliseconds
)