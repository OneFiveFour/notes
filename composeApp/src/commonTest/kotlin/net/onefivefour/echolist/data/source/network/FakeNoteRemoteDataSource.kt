package net.onefivefour.echolist.data.source.network

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

internal class FakeNoteRemoteDataSource : NoteRemoteDataSource {

    var createNoteResult: Result<CreateNoteResponse> = Result.success(CreateNoteResponse())
    var listNotesResult: Result<ListNotesResponse> = Result.success(ListNotesResponse())
    var getNoteResult: Result<GetNoteResponse> = Result.success(GetNoteResponse())
    var updateNoteResult: Result<UpdateNoteResponse> = Result.success(UpdateNoteResponse())
    var deleteNoteResult: Result<DeleteNoteResponse> = Result.success(DeleteNoteResponse())

    var lastCreateRequest: CreateNoteRequest? = null
    var lastListRequest: ListNotesRequest? = null
    var lastGetRequest: GetNoteRequest? = null
    var lastUpdateRequest: UpdateNoteRequest? = null
    var lastDeleteRequest: DeleteNoteRequest? = null

    override suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse {
        lastCreateRequest = request
        return createNoteResult.getOrThrow()
    }

    override suspend fun listNotes(request: ListNotesRequest): ListNotesResponse {
        lastListRequest = request
        return listNotesResult.getOrThrow()
    }

    override suspend fun getNote(request: GetNoteRequest): GetNoteResponse {
        lastGetRequest = request
        return getNoteResult.getOrThrow()
    }

    override suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse {
        lastUpdateRequest = request
        return updateNoteResult.getOrThrow()
    }

    override suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse {
        lastDeleteRequest = request
        return deleteNoteResult.getOrThrow()
    }
}
