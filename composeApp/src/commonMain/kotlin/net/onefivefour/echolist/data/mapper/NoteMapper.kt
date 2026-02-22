package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import notes.v1.CreateNoteRequest
import notes.v1.CreateNoteResponse
import notes.v1.GetNoteResponse
import notes.v1.UpdateNoteRequest

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

    fun toDomain(proto: CreateNoteResponse): Note = Note(
        filePath = proto.file_path,
        title = proto.title,
        content = proto.content,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: GetNoteResponse): Note = Note(
        filePath = proto.file_path,
        title = proto.title,
        content = proto.content,
        updatedAt = proto.updated_at
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
