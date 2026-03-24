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
            file_path = "/home/user/notes/meeting.md",
            title = "Meeting Notes",
            content = "Discussion about project timeline",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-1"
        domain.filePath shouldBe "/home/user/notes/meeting.md"
        domain.title shouldBe "Meeting Notes"
        domain.content shouldBe "Discussion about project timeline"
        domain.updatedAt shouldBe 1704067200000L
    }

    test("toDomain transforms CreateNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-2",
            file_path = "/home/user/notes/new.md",
            title = "New Note",
            content = "Fresh content",
            updated_at = 1704153600000L
        )
        val response = notes.v1.CreateNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-2"
        domain.filePath shouldBe "/home/user/notes/new.md"
        domain.title shouldBe "New Note"
        domain.content shouldBe "Fresh content"
        domain.updatedAt shouldBe 1704153600000L
    }

    test("toDomain transforms GetNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-3",
            file_path = "/home/user/notes/existing.md",
            title = "Existing Note",
            content = "Existing content",
            updated_at = 1704240000000L
        )
        val response = notes.v1.GetNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-3"
        domain.filePath shouldBe "/home/user/notes/existing.md"
        domain.title shouldBe "Existing Note"
        domain.content shouldBe "Existing content"
        domain.updatedAt shouldBe 1704240000000L
    }

    test("toDomain transforms UpdateNoteResponse to domain Note") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-4",
            file_path = "/home/user/notes/updated.md",
            title = "Updated Note",
            content = "Updated content",
            updated_at = 1704326400000L
        )
        val response = notes.v1.UpdateNoteResponse(note = protoNote)

        val domain = NoteMapper.toDomain(response)

        domain.id shouldBe "note-uuid-4"
        domain.filePath shouldBe "/home/user/notes/updated.md"
        domain.title shouldBe "Updated Note"
        domain.content shouldBe "Updated content"
        domain.updatedAt shouldBe 1704326400000L
    }

    test("toDomain transforms ListNotesResponse with multiple notes") {
        val note1 = notes.v1.Note(
            id = "note-uuid-5",
            file_path = "/home/user/notes/note1.md",
            title = "Note 1",
            content = "Content 1",
            updated_at = 1704067200000L
        )
        val note2 = notes.v1.Note(
            id = "note-uuid-6",
            file_path = "/home/user/notes/note2.md",
            title = "Note 2",
            content = "Content 2",
            updated_at = 1704153600000L
        )
        val response = notes.v1.ListNotesResponse(
            notes = listOf(note1, note2),
            entries = listOf("/home/user/notes/note1.md", "/home/user/notes/note2.md", "/home/user/notes/subfolder/")
        )

        val result = NoteMapper.toDomain(response)

        result.notes shouldHaveSize 2
        result.notes[0].id shouldBe "note-uuid-5"
        result.notes[0].filePath shouldBe "/home/user/notes/note1.md"
        result.notes[0].title shouldBe "Note 1"
        result.notes[1].id shouldBe "note-uuid-6"
        result.notes[1].filePath shouldBe "/home/user/notes/note2.md"
        result.notes[1].title shouldBe "Note 2"
        result.entries shouldHaveSize 3
        result.entries shouldBe listOf(
            "/home/user/notes/note1.md",
            "/home/user/notes/note2.md",
            "/home/user/notes/subfolder/"
        )
    }

    test("toDomain transforms ListNotesResponse with empty notes list") {
        val response = notes.v1.ListNotesResponse(
            notes = emptyList(),
            entries = emptyList()
        )

        val result = NoteMapper.toDomain(response)

        result.notes.shouldBeEmpty()
        result.entries.shouldBeEmpty()
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

    test("toProto transforms UpdateNoteParams to UpdateNoteRequest with id field") {
        val params = UpdateNoteParams(
            id = "note-uuid-existing",
            content = "Updated content here"
        )

        val proto = NoteMapper.toProto(params)

        proto.id shouldBe "note-uuid-existing"
        proto.content shouldBe "Updated content here"
    }

    // -- Edge cases --

    test("toDomain handles note with empty content") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-empty",
            file_path = "/home/user/notes/empty.md",
            title = "Empty Note",
            content = "",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-empty"
        domain.filePath shouldBe "/home/user/notes/empty.md"
        domain.title shouldBe "Empty Note"
        domain.content shouldBe ""
        domain.updatedAt shouldBe 1704067200000L
    }

    test("toDomain handles note with empty title") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-untitled",
            file_path = "/home/user/notes/untitled.md",
            title = "",
            content = "Some content",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-untitled"
        domain.filePath shouldBe "/home/user/notes/untitled.md"
        domain.title shouldBe ""
        domain.content shouldBe "Some content"
    }

    test("toDomain handles note with special characters in title and content") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-special",
            file_path = "/home/user/notes/special-chars_123.md",
            title = "Special: Title! @#$%",
            content = "Content with\nnewlines\tand\ttabs",
            updated_at = 1704067200000L
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.id shouldBe "note-uuid-special"
        domain.filePath shouldBe "/home/user/notes/special-chars_123.md"
        domain.title shouldBe "Special: Title! @#$%"
        domain.content shouldBe "Content with\nnewlines\tand\ttabs"
    }

    test("toDomain handles ListNotesResponse with single note") {
        val note = notes.v1.Note(
            id = "note-uuid-single",
            file_path = "/home/user/notes/single.md",
            title = "Single Note",
            content = "Single content",
            updated_at = 1704067200000L
        )
        val response = notes.v1.ListNotesResponse(
            notes = listOf(note),
            entries = listOf("/home/user/notes/single.md")
        )

        val result = NoteMapper.toDomain(response)

        result.notes shouldHaveSize 1
        result.notes[0].id shouldBe "note-uuid-single"
        result.notes[0].filePath shouldBe "/home/user/notes/single.md"
        result.entries shouldHaveSize 1
    }

    test("toDomain preserves timestamp precision") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-ts",
            file_path = "/home/user/notes/timestamp.md",
            title = "Timestamp Test",
            content = "Testing timestamp",
            updated_at = 1704067234567L // Millisecond precision
        )

        val domain = NoteMapper.toDomain(protoNote)

        domain.updatedAt shouldBe 1704067234567L
    }
})
