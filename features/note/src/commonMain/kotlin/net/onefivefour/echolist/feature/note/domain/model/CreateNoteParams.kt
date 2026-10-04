package net.onefivefour.echolist.feature.note.domain.model

internal data class CreateNoteParams(
    val title: String,
    val content: String,
    val parentDir: String
)