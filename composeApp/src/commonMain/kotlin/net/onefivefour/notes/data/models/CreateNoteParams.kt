package net.onefivefour.notes.data.models

data class CreateNoteParams(
    val title: String,
    val content: String,
    val path: String
)
