package net.onefivefour.notes.data.repository

import net.onefivefour.notes.data.models.CreateNoteParams
import net.onefivefour.notes.data.models.Note
import net.onefivefour.notes.data.models.UpdateNoteParams

/**
 * Repository abstraction for notes CRUD operations.
 * All operations are suspend functions returning [Result] types.
 */
interface NotesRepository {
    suspend fun createNote(params: CreateNoteParams): Result<Note>
    suspend fun listNotes(path: String = ""): Result<List<Note>>
    suspend fun getNote(filePath: String): Result<Note>
    suspend fun updateNote(params: UpdateNoteParams): Result<Note>
    suspend fun deleteNote(filePath: String): Result<Unit>
}
