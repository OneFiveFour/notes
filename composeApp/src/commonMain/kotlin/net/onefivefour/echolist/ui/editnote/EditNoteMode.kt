package net.onefivefour.echolist.ui.editnote

sealed interface EditNoteMode {
    data class Create(val parentPath: String) : EditNoteMode
    data class Edit(val noteId: String) : EditNoteMode
}
