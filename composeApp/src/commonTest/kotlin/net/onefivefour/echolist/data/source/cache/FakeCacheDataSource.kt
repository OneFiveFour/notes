package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.domain.model.Note

internal class FakeCacheDataSource : CacheDataSource {

    private val notes = mutableMapOf<String, Note>()
    private val entries = mutableMapOf<String, List<String>>()

    override suspend fun saveNote(note: Note) {
        notes[note.filePath] = note
    }

    override suspend fun saveNotes(notes: List<Note>) {
        notes.forEach { this.notes[it.filePath] = it }
    }

    override suspend fun getNote(filePath: String): Note? = notes[filePath]

    override suspend fun listNotes(path: String): List<Note> =
        notes.values.filter { it.filePath.startsWith(path) }

    override suspend fun deleteNote(filePath: String) {
        notes.remove(filePath)
    }

    override suspend fun saveEntries(parentPath: String, entries: List<String>) {
        this.entries[parentPath] = entries
    }

    override suspend fun listEntries(parentPath: String): List<String> =
        entries[parentPath] ?: emptyList()

    override suspend fun clear() {
        notes.clear()
        entries.clear()
    }
}