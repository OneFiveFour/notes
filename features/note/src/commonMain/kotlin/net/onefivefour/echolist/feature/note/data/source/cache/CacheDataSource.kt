package net.onefivefour.echolist.feature.note.data.source.cache

import net.onefivefour.echolist.feature.note.domain.model.Note

internal interface CacheDataSource {
    // Notes
    suspend fun saveNote(note: Note)
    suspend fun saveNotes(notes: List<Note>)
    suspend fun getNote(id: String): Note?
    suspend fun listNotes(parentDir: String): List<Note>
    suspend fun deleteNote(id: String)

    // Folders
    suspend fun saveEntries(parentDir: String, entries: List<String>)
    suspend fun listEntries(parentDir: String): List<String>
    suspend fun clear()
}
