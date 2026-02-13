package net.onefivefour.notes.data.source.network

import net.onefivefour.notes.network.client.ConnectRpcClient
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

internal class NetworkDataSourceImpl(
    private val client: ConnectRpcClient
) : NetworkDataSource {

    override suspend fun createNote(request: CreateNoteRequest): CreateNoteResponse {
        return client.call(
            path = "/notes.v1.NotesService/CreateNote",
            request = request,
            requestSerializer = { CreateNoteRequest.ADAPTER.encode(it) },
            responseDeserializer = { CreateNoteResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun listNotes(request: ListNotesRequest): ListNotesResponse {
        return client.call(
            path = "/notes.v1.NotesService/ListNotes",
            request = request,
            requestSerializer = { ListNotesRequest.ADAPTER.encode(it) },
            responseDeserializer = { ListNotesResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun getNote(request: GetNoteRequest): GetNoteResponse {
        return client.call(
            path = "/notes.v1.NotesService/GetNote",
            request = request,
            requestSerializer = { GetNoteRequest.ADAPTER.encode(it) },
            responseDeserializer = { GetNoteResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun updateNote(request: UpdateNoteRequest): UpdateNoteResponse {
        return client.call(
            path = "/notes.v1.NotesService/UpdateNote",
            request = request,
            requestSerializer = { UpdateNoteRequest.ADAPTER.encode(it) },
            responseDeserializer = { UpdateNoteResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }

    override suspend fun deleteNote(request: DeleteNoteRequest): DeleteNoteResponse {
        return client.call(
            path = "/notes.v1.NotesService/DeleteNote",
            request = request,
            requestSerializer = { DeleteNoteRequest.ADAPTER.encode(it) },
            responseDeserializer = { DeleteNoteResponse.ADAPTER.decode(it) }
        ).getOrThrow()
    }
}
