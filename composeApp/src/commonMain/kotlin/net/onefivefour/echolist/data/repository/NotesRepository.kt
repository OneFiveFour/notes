package net.onefivefour.echolist.data.repository

import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.ListNotesResult
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams

/**
 * Repository abstraction for notes CRUD operations.
 * All operations are suspend functions returning [Result] types.
 */
interface NotesRepository {
    suspend fun createNote(params: CreateNoteParams): Result<Note>
    suspend fun listNotes(path: String = ""): Result<ListNotesResult>
    suspend fun getNote(filePath: String): Result<Note>
    suspend fun updateNote(params: UpdateNoteParams): Result<Note>
    suspend fun deleteNote(filePath: String): Result<Unit>
}
