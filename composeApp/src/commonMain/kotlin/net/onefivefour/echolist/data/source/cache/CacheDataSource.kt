package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.data.models.Note

internal interface CacheDataSource {
    suspend fun saveNote(note: Note)
    suspend fun saveNotes(notes: List<Note>)
    suspend fun getNote(filePath: String): Note?
    suspend fun listNotes(path: String): List<Note>
    suspend fun deleteNote(filePath: String)
    suspend fun saveEntries(parentPath: String, entries: List<String>)
    suspend fun listEntries(parentPath: String): List<String>
    suspend fun clear()
}
