package net.onefivefour.echolist.ui.editnote

sealed interface EditNoteMode {
    data class Create(val parentDir: String) : EditNoteMode
    data class Edit(val noteId: String) : EditNoteMode
}
