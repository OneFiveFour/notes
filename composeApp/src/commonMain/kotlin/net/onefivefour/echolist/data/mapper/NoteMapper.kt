package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.ListNotesResult
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import notes.v1.CreateNoteRequest
import notes.v1.CreateNoteResponse
import notes.v1.GetNoteResponse
import notes.v1.ListNotesResponse
import notes.v1.UpdateNoteRequest
import notes.v1.UpdateNoteResponse

/**
 * Maps between Wire-generated proto models and domain models.
 * Timestamps are preserved as Unix milliseconds (Long) with no conversion.
 */
internal object NoteMapper {

    // Proto -> Domain

    fun toDomain(proto: notes.v1.Note): Note = Note(
        filePath = proto.file_path,
        title = proto.title,
        content = proto.content,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: CreateNoteResponse): Note = toDomain(proto.note!!)

    fun toDomain(proto: GetNoteResponse): Note = toDomain(proto.note!!)

    fun toDomain(proto: UpdateNoteResponse): Note = toDomain(proto.note!!)

    fun toDomain(proto: ListNotesResponse): ListNotesResult = ListNotesResult(
        notes = proto.notes.map { toDomain(it) },
        entries = proto.entries
    )

    // Domain -> Proto

    fun toProto(params: CreateNoteParams): CreateNoteRequest = CreateNoteRequest(
        title = params.title,
        content = params.content,
        path = params.path
    )

    fun toProto(params: UpdateNoteParams): UpdateNoteRequest = UpdateNoteRequest(
        file_path = params.filePath,
        content = params.content
    )
}
