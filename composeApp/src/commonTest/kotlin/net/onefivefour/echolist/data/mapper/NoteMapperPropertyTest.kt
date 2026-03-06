package net.onefivefour.echolist.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.echolist.data.models.Note

/**
 * Feature: proto-api-update
 * Property 8: NoteMapper transforms Note proto messages correctly
 * Property 9: NoteMapper transforms Note response messages correctly
 * Property 10: NoteMapper transforms ListNotesResponse correctly
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5
 */
class NoteMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoNote = arbitrary {
        notes.v1.Note(
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            content = Arb.string(0..500).bind(),
            updated_at = Arb.long(0..Long.MAX_VALUE).bind()
        )
    }

    val arbCreateNoteResponse = arbitrary {
        notes.v1.CreateNoteResponse(
            note = arbProtoNote.bind()
        )
    }

    val arbGetNoteResponse = arbitrary {
        notes.v1.GetNoteResponse(
            note = arbProtoNote.bind()
        )
    }

    val arbUpdateNoteResponse = arbitrary {
        notes.v1.UpdateNoteResponse(
            note = arbProtoNote.bind()
        )
    }

    val arbListNotesResponse = arbitrary {
        val notesList = Arb.list(arbProtoNote, 0..100).bind()
        val entriesList = Arb.list(Arb.string(1..100), 0..100).bind()
        notes.v1.ListNotesResponse(
            notes = notesList,
            entries = entriesList
        )
    }

    // -- Property 8: NoteMapper transforms Note proto messages correctly --

    test("Feature: proto-api-update, Property 8: NoteMapper transforms Note proto messages correctly") {
        checkAll(PropTestConfig(iterations = 100), arbProtoNote) { proto ->
            val domain = NoteMapper.toDomain(proto)

            // Verify file_path -> filePath mapping
            domain.filePath shouldBe proto.file_path

            // Verify all other fields correctly mapped
            domain.title shouldBe proto.title
            domain.content shouldBe proto.content
            domain.updatedAt shouldBe proto.updated_at
        }
    }

    // -- Property 9: NoteMapper transforms Note response messages correctly --

    test("Feature: proto-api-update, Property 9: CreateNoteResponse -> Note domain model preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbCreateNoteResponse) { response ->
            val domain = NoteMapper.toDomain(response)
            val protoNote = response.note!!

            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 9: GetNoteResponse -> Note domain model preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbGetNoteResponse) { response ->
            val domain = NoteMapper.toDomain(response)
            val protoNote = response.note!!

            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 9: UpdateNoteResponse -> Note domain model preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbUpdateNoteResponse) { response ->
            val domain = NoteMapper.toDomain(response)
            val protoNote = response.note!!

            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    // -- Property 10: NoteMapper transforms ListNotesResponse correctly --

    test(
        "Feature: proto-api-update, Property 10: ListNotesResponse -> ListNotesResult preserves all notes and entries"
    ) {
        checkAll(PropTestConfig(iterations = 100), arbListNotesResponse) { response ->
            val result = NoteMapper.toDomain(response)

            // Verify all notes transformed
            result.notes shouldHaveSize response.notes.size

            // Verify order and count preserved
            result.notes.forEachIndexed { index, note ->
                val protoNote = response.notes[index]
                note.filePath shouldBe protoNote.file_path
                note.title shouldBe protoNote.title
                note.content shouldBe protoNote.content
                note.updatedAt shouldBe protoNote.updated_at
            }

            // Verify entries field correctly mapped
            result.entries shouldHaveSize response.entries.size
            result.entries shouldBe response.entries
        }
    }
})