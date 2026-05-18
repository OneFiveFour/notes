package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.UpdateNoteParams

/**
 * Unit tests for NoteMapper transformations.
 * Tests specific examples, edge cases, and field name conversions.
 */
class NoteMapperTest : FunSpec({

    // -- Proto -> Domain transformations --

    test("toDomain transforms proto Note to domain Note with field name conversions") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-1",
            parent_dir = "home/user/notes",
            title = "Meeting Notes",
            content = "Discussion about project timeline",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-1"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "Meeting Notes"
        domain.content shouldBe "Discussion about project timeline"
        domain.updatedAt shouldBe 1704067200000L
    }

    test("toDomain transforms CreateNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-2",
            parent_dir = "home/user/notes",
            title = "New Note",
            content = "Fresh content",
            updated_at = 1704153600000L
        )
        val response = notes.v1.CreateNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-2"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "New Note"
        domain.content shouldBe "Fresh content"
        domain.updatedAt shouldBe 1704153600000L
    }

    test("toDomain transforms GetNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-3",
            parent_dir = "home/user/notes",
            title = "Existing Note",
            content = "Existing content",
            updated_at = 1704240000000L
        )
        val response = notes.v1.GetNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-3"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "Existing Note"
        domain.content shouldBe "Existing content"
        domain.updatedAt shouldBe 1704240000000L
    }

    test("toDomain transforms UpdateNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-4",
            parent_dir = "home/user/notes",
            title = "Updated Note",
            content = "Updated content",
            updated_at = 1704326400000L
        )
        val response = notes.v1.UpdateNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-4"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "Updated Note"
        domain.content shouldBe "Updated content"
        domain.updatedAt shouldBe 1704326400000L
    }

    test("toDomain transforms ListNotesResponse with multiple notes") {
        val note1 = notes.v1.Note(
            id = "note-uuid-5",
            parent_dir = "home/user/notes",
            title = "Note 1",
            content = "Content 1",
            updated_at = 1704067200000L
        )
        val note2 = notes.v1.Note(
            id = "note-uuid-6",
            parent_dir = "home/user/notes",
            title = "Note 2",
            content = "Content 2",
            updated_at = 1704153600000L
        )
        val response = notes.v1.ListNotesResponse(
            notes = listOf(note1, note2)
        )

        val result = NoteMapper.toDomain(response)

        result shouldHaveSize 2
        result[0].id shouldBe "note-uuid-5"
        result[0].parentDir shouldBe "home/user/notes"
        result[0].title shouldBe "Note 1"
        result[1].id shouldBe "note-uuid-6"
        result[1].parentDir shouldBe "home/user/notes"
        result[1].title shouldBe "Note 2"
    }

    test("toDomain transforms ListNotesResponse with empty notes list") {
        val response = notes.v1.ListNotesResponse(
            notes = emptyList()
        )

        val result = NoteMapper.toDomain(response)

        result.shouldBeEmpty()
    }

    // -- Domain -> Proto transformations --

    test("toProto transforms CreateNoteParams to CreateNoteRequest with snake_case fields") {
        val params = CreateNoteParams(
            title = "New Note Title",
            content = "New note content",
            parentDir = "/home/user/notes"
        )

        val proto = NoteMapper.toProto(params)

        proto.title shouldBe "New Note Title"
        proto.content shouldBe "New note content"
        proto.parent_dir shouldBe "/home/user/notes"
    }

    test("toProto transforms UpdateNoteParams to UpdateNoteRequest with id and title fields") {
        val params = UpdateNoteParams(
            id = "note-uuid-existing",
            title = "Updated Title",
            content = "Updated content here"
        )

        val proto = NoteMapper.toProto(params)

        proto.id shouldBe "note-uuid-existing"
        proto.title shouldBe "Updated Title"
        proto.content shouldBe "Updated content here"
    }

    // -- Edge cases --

    test("toDomain handles note with empty content") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-empty",
            parent_dir = "home/user/notes",
            title = "Empty Note",
            content = "",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-empty"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "Empty Note"
        domain.content shouldBe ""
        domain.updatedAt shouldBe 1704067200000L
    }

    test("toDomain handles note with empty title") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-untitled",
            parent_dir = "home/user/notes",
            title = "",
            content = "Some content",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-untitled"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe ""
        domain.content shouldBe "Some content"
    }

    test("toDomain handles note with special characters in title and content") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-special",
            parent_dir = "home/user/notes",
            title = "Special: Title! @#\$%",
            content = "Content with\nnewlines\tand\ttabs",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-special"
        domain.parentDir shouldBe "home/user/notes"
        domain.title shouldBe "Special: Title! @#\$%"
        domain.content shouldBe "Content with\nnewlines\tand\ttabs"
    }

    test("toDomain handles ListNotesResponse with single note") {
        val note = notes.v1.Note(
            id = "note-uuid-single",
            parent_dir = "home/user/notes",
            title = "Single Note",
            content = "Single content",
            updated_at = 1704067200000L
        )
        val response = notes.v1.ListNotesResponse(
            notes = listOf(note)
        )

        val result = NoteMapper.toDomain(response)

        result shouldHaveSize 1
        result[0].id shouldBe "note-uuid-single"
        result[0].parentDir shouldBe "home/user/notes"
    }

    test("toDomain preserves timestamp precision") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-ts",
            parent_dir = "home/user/notes",
            title = "Timestamp Test",
            content = "Testing timestamp",
            updated_at = 1704067234567L // Millisecond precision
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.updatedAt shouldBe 1704067234567L
    }
})
