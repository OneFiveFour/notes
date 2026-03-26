package net.onefivefour.echolist.data.mapper

import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.domain.model.Note
import net.onefivefour.echolist.data.dto.UpdateNoteParams
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
        id = proto.id,
        filePath = proto.file_path,
        title = proto.title,
        content = proto.content,
        updatedAt = proto.updated_at
    )

    fun toDomain(proto: CreateNoteResponse): Note = toDomain(requireNote(proto.note, "CreateNoteResponse"))

    fun toDomain(proto: GetNoteResponse): Note = toDomain(requireNote(proto.note, "GetNoteResponse"))

    fun toDomain(proto: UpdateNoteResponse): Note = toDomain(requireNote(proto.note, "UpdateNoteResponse"))

    fun toDomain(proto: ListNotesResponse): List<Note> = proto.notes.map { toDomain(it) }

    // Domain -> Proto

    fun toProto(params: CreateNoteParams): CreateNoteRequest = CreateNoteRequest(
        title = params.title,
        content = params.content,
        parent_dir = params.parentDir
    )

    fun toProto(params: UpdateNoteParams): UpdateNoteRequest = UpdateNoteRequest(
        id = params.id,
        title = params.title,
        content = params.content
    )

    private fun requireNote(note: notes.v1.Note?, responseType: String): notes.v1.Note =
        requireNotNull(note) { "$responseType did not include a note payload" }
}
