package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.domain.model.Note

internal class CacheDataSourceImpl(
    private val database: EchoListDatabase,
    private val currentTimeMillis: () -> Long = { currentEpochMillis() }
) : CacheDataSource {

    private val noteQueries get() = database.notesQueries
    private val entryQueries get() = database.folderQueries

    override suspend fun saveNote(note: Note) {
        noteQueries.insertOrReplace(
            id = note.id,
            parentDir = note.parentDir,
            title = note.title,
            content = note.content,
            updatedAt = note.updatedAt,
            cachedAt = currentTimeMillis()
        )
    }

    override suspend fun saveNotes(notes: List<Note>) {
        database.transaction {
            notes.forEach { note ->
                noteQueries.insertOrReplace(
                    id = note.id,
                    parentDir = note.parentDir,
                    title = note.title,
                    content = note.content,
                    updatedAt = note.updatedAt,
                    cachedAt = currentTimeMillis()
                )
            }
        }
    }

    override suspend fun getNote(id: String): Note? {
        return noteQueries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun listNotes(parentDir: String): List<Note> {
        return if (parentDir.isEmpty()) {
            noteQueries.selectAll().executeAsList().map { it.toDomain() }
        } else {
            noteQueries.selectByParentDir(parentDir).executeAsList().map { it.toDomain() }
        }
    }

    override suspend fun deleteNote(id: String) {
        noteQueries.deleteById(id)
    }

    override suspend fun saveEntries(parentDir: String, entries: List<String>) {
        database.transaction {
            entryQueries.deleteByParentPath(parentDir)
            val now = currentTimeMillis()
            entries.forEach { entryPath ->
                entryQueries.insertOrReplace(
                    parentPath = parentDir,
                    entryPath = entryPath,
                    cachedAt = now
                )
            }
        }
    }

    override suspend fun listEntries(parentDir: String): List<String> {
        return entryQueries.selectByParentPath(parentDir).executeAsList()
    }

    override suspend fun clear() {
        database.transaction {
            noteQueries.deleteAll()
            entryQueries.deleteAll()
        }
    }
}

private fun net.onefivefour.echolist.cache.Note.toDomain(): Note {
    return Note(
        id = id,
        parentDir = parentDir,
        title = title,
        content = content,
        updatedAt = updatedAt
    )
}