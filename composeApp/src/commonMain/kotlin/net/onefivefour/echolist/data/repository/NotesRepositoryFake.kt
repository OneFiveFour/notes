package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.ListNotesResult
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

    private val entries = mutableListOf<String>()

    /** Pre-populate the in-memory store with notes. */
    fun addNotes(vararg notesToAdd: Note) {
        notesToAdd.forEach { notes[it.filePath] = it }
    }

    /** Pre-populate the entries list. */
    fun addEntries(vararg entriesToAdd: String) {
        entries.addAll(entriesToAdd)
    }

    override suspend fun createNote(params: CreateNoteParams): Result<Note> {
        callLog.add("createNote(${params.title}, ${params.content}, ${params.parentDir})")
        shouldFail?.let { return Result.failure(it) }

        val note = Note(
            filePath = "${params.parentDir}/${params.title}",
            title = params.title,
            content = params.content,
            updatedAt = 0L
        )
        notes[note.filePath] = note
        return Result.success(note)
    }

    override suspend fun listNotes(parentDir: String): Result<ListNotesResult> {
        callLog.add("listNotes($parentDir)")
        shouldFail?.let { return Result.failure(it) }

        val filteredNotes = if (parentDir.isEmpty()) {
            notes.values.toList()
        } else {
            notes.values.filter { it.filePath.startsWith(parentDir) }
        }
        return Result.success(ListNotesResult(notes = filteredNotes, entries = entries.toList()))
    }

    override suspend fun getNote(filePath: String): Result<Note> {
        callLog.add("getNote($filePath)")
        shouldFail?.let { return Result.failure(it) }

        val note = notes[filePath]
            ?: return Result.failure(NoSuchElementException("Note not found: $filePath"))
        return Result.success(note)
    }

    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> {
        callLog.add("updateNote(${params.filePath}, ${params.content})")
        shouldFail?.let { return Result.failure(it) }

        val existing = notes[params.filePath]
            ?: return Result.failure(NoSuchElementException("Note not found: ${params.filePath}"))

        val updated = existing.copy(content = params.content, updatedAt = existing.updatedAt + 1)
        notes[updated.filePath] = updated
        return Result.success(updated)
    }

    override suspend fun deleteNote(filePath: String): Result<Unit> {
        callLog.add("deleteNote($filePath)")
        shouldFail?.let { return Result.failure(it) }

        notes.remove(filePath)
        return Result.success(Unit)
    }
}