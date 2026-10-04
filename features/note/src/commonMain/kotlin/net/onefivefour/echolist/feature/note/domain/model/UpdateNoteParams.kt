package net.onefivefour.echolist.feature.note.domain.model

internal data class UpdateNoteParams(
    val id: String,
    val title: String,
    val content: String
)
