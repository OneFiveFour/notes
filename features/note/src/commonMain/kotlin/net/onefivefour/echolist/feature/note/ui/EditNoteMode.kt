package net.onefivefour.echolist.feature.note.ui

internal sealed interface EditNoteMode {
    data class Create(val parentDir: String) : EditNoteMode
    data class Edit(val noteId: String) : EditNoteMode
}