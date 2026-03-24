package net.onefivefour.echolist.domain.repository

import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.domain.model.Note
import net.onefivefour.echolist.data.dto.UpdateNoteParams

/**
 * Repository abstraction for notes CRUD operations.
 * All operations are suspend functions returning [Result] types.
 */
interface NotesRepository {
    suspend fun createNote(params: CreateNoteParams): Result<Note>
    suspend fun listNotes(parentDir: String = ""): Result<List<Note>>
    suspend fun getNote(noteId: String): Result<Note>
    suspend fun updateNote(params: UpdateNoteParams): Result<Note>
    suspend fun deleteNote(noteId: String): Result<Unit>
}
