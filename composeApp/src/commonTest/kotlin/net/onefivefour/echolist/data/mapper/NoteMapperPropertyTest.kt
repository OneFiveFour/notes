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

/**
 * Feature: proto-api-update
 * Property 3: Note mapper response-to-domain field preservation
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4
 */
class NoteMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoNote = arbitrary {
        notes.v1.Note(
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(0..200).bind(),
            content = Arb.string(0..500).bind(),
            updated_at = Arb.long(0L..Long.MAX_VALUE).bind()
        )
    }

    // -- Property 3: Response -> Domain field preservation --

    test("Feature: proto-api-update, Property 3: CreateNoteResponse -> domain Note preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoNote) { protoNote ->
            val response = notes.v1.CreateNoteResponse(note = protoNote)
            val domain = NoteMapper.toDomain(response)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 3: GetNoteResponse -> domain Note preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoNote) { protoNote ->
            val response = notes.v1.GetNoteResponse(note = protoNote)
            val domain = NoteMapper.toDomain(response)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 3: UpdateNoteResponse -> domain Note preserves all fields") {
        checkAll(PropTestConfig(iterations = 100), arbProtoNote) { protoNote ->
            val response = notes.v1.UpdateNoteResponse(note = protoNote)
            val domain = NoteMapper.toDomain(response)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 3: ListNotesResponse -> ListNotesResult preserves notes count, entries count, and all field values") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbProtoNote, 0..10),
            Arb.list(Arb.string(1..100), 0..10)
        ) { protoNotes, entries ->
            val response = notes.v1.ListNotesResponse(notes = protoNotes, entries = entries)
            val result = NoteMapper.toDomain(response)
            result.notes shouldHaveSize protoNotes.size
            result.entries shouldHaveSize entries.size
            result.entries shouldBe entries
            result.notes.zip(protoNotes).forEach { (domain, proto) ->
                domain.filePath shouldBe proto.file_path
                domain.title shouldBe proto.title
                domain.content shouldBe proto.content
                domain.updatedAt shouldBe proto.updated_at
            }
        }
    }
})
