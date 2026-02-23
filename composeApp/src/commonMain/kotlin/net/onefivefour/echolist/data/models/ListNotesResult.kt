package net.onefivefour.echolist.data.models

/**
 * Result of listing notes at a given path.
 * @param notes Full note objects for all notes in the subtree.
 * @param entries Immediate children paths. Folder paths end with "/", note paths don't.
 */
data class ListNotesResult(
    val notes: List<Note>,
    val entries: List<String>
)
