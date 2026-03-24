package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.domain.model.Note
import net.onefivefour.echolist.data.dto.UpdateNoteParams
import net.onefivefour.echolist.domain.repository.NotesRepository

/**
 * Fake [net.onefivefour.echolist.domain.repository.NotesRepository] for testing. Stores notes in memory,
 * supports pre-configured error simulation, and tracks all method calls.
 */
class NotesRepositoryFake : NotesRepository {

    private val notes = mutableMapOf<String, Note>()
    private var shouldFail: Exception? = null

    /** All method invocations recorded as "methodName(args…)" strings. */
    val callLog = mutableListOf<String>()

    /**
     * When set to a non-null exception, every subsequent call will
     * return [Result.failure] with that exception.
     * Pass `null` to clear.
     */
    fun setShouldFail(exception: Exception?) {
        shouldFail = exception
    }

    /** Pre-populate the in-memory store with notes. */
    fun addNotes(vararg notesToAdd: Note) {
        notesToAdd.forEach { notes[it.id] = it }
    }

    override suspend fun createNote(params: CreateNoteParams): Result<Note> {
        callLog.add("createNote(${params.title}, ${params.content}, ${params.parentDir})")
        shouldFail?.let { return Result.failure(it) }

        val note = Note(
            id = "generated-${params.title}",
            filePath = joinPath(params.parentDir, params.title),
            title = params.title,
            content = params.content,
            updatedAt = 0L
        )
        notes[note.id] = note
        return Result.success(note)
    }

    override suspend fun listNotes(parentDir: String): Result<List<Note>> {
        callLog.add("listNotes($parentDir)")
        shouldFail?.let { return Result.failure(it) }

        val filteredNotes = if (parentDir.isEmpty()) {
            notes.values.toList()
        } else {
            notes.values.filter { it.filePath.startsWith(parentDir) }
        }
        return Result.success(filteredNotes)
    }

    override suspend fun getNote(noteId: String): Result<Note> {
        callLog.add("getNote($noteId)")
        shouldFail?.let { return Result.failure(it) }

        val note = notes[noteId]
            ?: return Result.failure(NoSuchElementException("Note not found: $noteId"))
        return Result.success(note)
    }

    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> {
        callLog.add("updateNote(${params.id}, ${params.content})")
        shouldFail?.let { return Result.failure(it) }

        val existing = notes[params.id]
            ?: return Result.failure(NoSuchElementException("Note not found: ${params.id}"))

        val updated = existing.copy(content = params.content, updatedAt = existing.updatedAt + 1)
        notes[updated.id] = updated
        return Result.success(updated)
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        callLog.add("deleteNote($noteId)")
        shouldFail?.let { return Result.failure(it) }

        notes.remove(noteId)
        return Result.success(Unit)
    }
}
