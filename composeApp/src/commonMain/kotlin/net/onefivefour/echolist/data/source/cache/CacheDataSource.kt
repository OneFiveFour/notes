package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.domain.model.Note

internal interface CacheDataSource {
    // Notes
    suspend fun saveNote(note: Note)
    suspend fun saveNotes(notes: List<Note>)
    suspend fun getNote(id: String): Note?
    suspend fun listNotes(path: String): List<Note>
    suspend fun deleteNote(id: String)

    // Folders
    suspend fun saveEntries(parentPath: String, entries: List<String>)
    suspend fun listEntries(parentPath: String): List<String>
    suspend fun clear()
}
