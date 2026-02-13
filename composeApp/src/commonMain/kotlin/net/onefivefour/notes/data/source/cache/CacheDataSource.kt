package net.onefivefour.notes.data.source.cache

import net.onefivefour.notes.data.models.Note

internal interface CacheDataSource {
    suspend fun saveNote(note: Note)
    suspend fun saveNotes(notes: List<Note>)
    suspend fun getNote(filePath: String): Note?
    suspend fun listNotes(path: String): List<Note>
    suspend fun deleteNote(filePath: String)
    suspend fun clear()
}
