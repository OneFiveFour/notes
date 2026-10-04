package net.onefivefour.echolist.feature.note.data.source.cache

import net.onefivefour.echolist.core.database.data.DatabaseProvider
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.async.coroutines.awaitAsList
import net.onefivefour.echolist.feature.note.domain.model.Note

internal class CacheDataSourceImpl(
    private val databaseProvider: DatabaseProvider,
    private val currentTimeMillis: () -> Long = { currentEpochMillis() }
) : CacheDataSource {

    override suspend fun saveNote(note: Note) {
        val database = databaseProvider.get()
        database.notesQueries.insertOrReplace(
            id = note.id,
            parentDir = note.parentDir,
            title = note.title,
            content = note.content,
            updatedAt = note.updatedAt,
            cachedAt = currentTimeMillis()
        )
    }

    override suspend fun saveNotes(notes: List<Note>) {
        val database = databaseProvider.get()
        database.transaction {
            notes.forEach { note ->
                database.notesQueries.insertOrReplace(
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
        val database = databaseProvider.get()
        return database.notesQueries.selectById(id).awaitAsOneOrNull()?.toDomain()
    }

    override suspend fun listNotes(parentDir: String): List<Note> {
        val database = databaseProvider.get()
        return if (parentDir.isEmpty()) {
            database.notesQueries.selectAll().awaitAsList().map { it.toDomain() }
        } else {
            database.notesQueries.selectByParentDir(parentDir).awaitAsList().map { it.toDomain() }
        }
    }

    override suspend fun deleteNote(id: String) {
        val database = databaseProvider.get()
        database.notesQueries.deleteById(id)
    }

    override suspend fun saveEntries(parentDir: String, entries: List<String>) {
        val database = databaseProvider.get()
        database.transaction {
            database.folderQueries.deleteByParentPath(parentDir)
            val now = currentTimeMillis()
            entries.forEach { entryPath ->
                database.folderQueries.insertOrReplace(
                    parentPath = parentDir,
                    entryPath = entryPath,
                    cachedAt = now
                )
            }
        }
    }

    override suspend fun listEntries(parentDir: String): List<String> {
        val database = databaseProvider.get()
        return database.folderQueries.selectByParentPath(parentDir).awaitAsList()
    }

    override suspend fun clear() {
        val database = databaseProvider.get()
        database.transaction {
            database.notesQueries.deleteAll()
            database.folderQueries.deleteAll()
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