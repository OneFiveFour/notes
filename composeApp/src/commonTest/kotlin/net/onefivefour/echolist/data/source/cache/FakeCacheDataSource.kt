package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.domain.model.Note

internal class FakeCacheDataSource : CacheDataSource {

    private val notes = mutableMapOf<String, Note>()
    private val entries = mutableMapOf<String, List<String>>()

    override suspend fun saveNote(note: Note) {
        notes[note.id] = note
    }

    override suspend fun saveNotes(notes: List<Note>) {
        notes.forEach { this.notes[it.id] = it }
    }

    override suspend fun getNote(id: String): Note? = notes[id]

    override suspend fun listNotes(parentDir: String): List<Note> =
        if (parentDir.isEmpty()) {
            notes.values.toList()
        } else {
            notes.values.filter { it.parentDir == parentDir }
        }

    override suspend fun deleteNote(id: String) {
        notes.remove(id)
    }

    override suspend fun saveEntries(parentDir: String, entries: List<String>) {
        this.entries[parentDir] = entries
    }

    override suspend fun listEntries(parentDir: String): List<String> =
        entries[parentDir] ?: emptyList()

    override suspend fun clear() {
        notes.clear()
        entries.clear()
    }
}
