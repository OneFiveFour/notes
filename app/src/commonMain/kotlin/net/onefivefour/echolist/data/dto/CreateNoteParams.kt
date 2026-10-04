package net.onefivefour.echolist.data.dto

data class CreateNoteParams(
    val title: String,
    val content: String,
    val parentDir: String
)