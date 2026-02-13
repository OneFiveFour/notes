package net.onefivefour.notes.data.source.network

import notes.v1.CreateNoteRequest
import notes.v1.CreateNoteResponse
import notes.v1.DeleteNoteRequest
import notes.v1.DeleteNoteResponse
import notes.v1.GetNoteRequest
import notes.v1.GetNoteResponse
import notes.v1.ListNotesRequest
import notes.v1.ListNotesResponse
import notes.v1.UpdateNoteRequest
import notes.v1.UpdateNoteResponse

internal interface NetworkDataSource {
    suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse
    suspend fun listNotes(request: ListNotesRequest): ListNotesResponse
    suspend fun getNote(request: GetNoteRequest): GetNoteResponse
    suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse
    suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse
}
