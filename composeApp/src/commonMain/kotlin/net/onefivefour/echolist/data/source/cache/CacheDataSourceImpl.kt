package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.models.Note

internal class CacheDataSourceImpl(
    private val database: EchoListDatabase,
    private val currentTimeMillis: () -> Long = { currentEpochMillis() }
) : CacheDataSource {

    private val noteQueries get() = database.notesQueries
    private val entryQueries get() = database.folderQueries

    override suspend fun saveNote(note: Note) {
        noteQueries.insertOrReplace(
            filePath = note.filePath,
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
                    filePath = note.filePath,
                    title = note.title,
                    content = note.content,
                    updatedAt = note.updatedAt,
                    cachedAt = currentTimeMillis()
                )
            }
        }
    }

    override suspend fun getNote(filePath: String): Note? {
        return noteQueries.selectByFilePath(filePath).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun listNotes(path: String): List<Note> {
        return if (path.isEmpty()) {
            noteQueries.selectAll().executeAsList().map { it.toDomain() }
        } else {
            noteQueries.selectByPathPrefix(path).executeAsList().map { it.toDomain() }
        }
    }

    override suspend fun deleteNote(filePath: String) {
        noteQueries.deleteByFilePath(filePath)
    }

    override suspend fun saveEntries(parentPath: String, entries: List<String>) {
        database.transaction {
            entryQueries.deleteByParentPath(parentPath)
            val now = currentTimeMillis()
            entries.forEach { entryPath ->
                entryQueries.insertOrReplace(
                    parentPath = parentPath,
                    entryPath = entryPath,
                    cachedAt = now
                )
            }
        }
    }

    override suspend fun listEntries(parentPath: String): List<String> {
        return entryQueries.selectByParentPath(parentPath).executeAsList()
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
        filePath = filePath,
        title = title,
        content = content,
        updatedAt = updatedAt
    )
}
