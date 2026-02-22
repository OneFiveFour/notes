package net.onefivefour.echolist.data.source.cache

import net.onefivefour.echolist.cache.EchoListDatabase
import net.onefivefour.echolist.data.models.Note

internal class CacheDataSourceImpl(
    private val database: EchoListDatabase,
    private val currentTimeMillis: () -> Long = { currentEpochMillis() }
) : CacheDataSource {

    private val queries get() = database.notesQueries

    override suspend fun saveNote(note: Note) {
        queries.insertOrReplace(
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
                queries.insertOrReplace(
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
        return queries.selectByFilePath(filePath).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun listNotes(path: String): List<Note> {
        return if (path.isEmpty()) {
            queries.selectAll().executeAsList().map { it.toDomain() }
        } else {
            queries.selectByPathPrefix(path).executeAsList().map { it.toDomain() }
        }
    }

    override suspend fun deleteNote(filePath: String) {
        queries.deleteByFilePath(filePath)
    }

    override suspend fun clear() {
        queries.deleteAll()
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
