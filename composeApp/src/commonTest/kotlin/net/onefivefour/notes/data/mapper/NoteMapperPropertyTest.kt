package net.onefivefour.notes.data.mapper

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import net.onefivefour.notes.data.models.CreateNoteParams
import net.onefivefour.notes.data.models.Note
import net.onefivefour.notes.data.models.UpdateNoteParams

/**
 * Property-based tests for NoteMapper round-trip conversions.
 *
 * **Validates: Requirements 3.3, 3.4, 3.5**
 */
class NoteMapperPropertyTest : FunSpec({

    // -- Generators --

    val arbNote = arbitrary {
        Note(
            filePath = Arb.string(1..100).bind(),
            title = Arb.string(0..200).bind(),
            content = Arb.string(0..500).bind(),
            updatedAt = Arb.long(0L..Long.MAX_VALUE).bind()
        )
    }

    val arbProtoNote = arbitrary {
        notes.v1.Note(
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(0..200).bind(),
            content = Arb.string(0..500).bind(),
            updated_at = Arb.long(0L..Long.MAX_VALUE).bind()
        )
    }

    val arbCreateNoteParams = arbitrary {
        CreateNoteParams(
            title = Arb.string(0..200).bind(),
            content = Arb.string(0..500).bind(),
            path = Arb.string(1..100).bind()
        )
    }

    val arbUpdateNoteParams = arbitrary {
        UpdateNoteParams(
            filePath = Arb.string(1..100).bind(),
            content = Arb.string(0..500).bind()
        )
    }

    val arbTimestamp = Arb.long(0L..Long.MAX_VALUE)


    // -- Property 4: Domain-Proto Mapping Round-Trip --

    test("Property 4: Proto Note -> Domain Note preserves all fields").config(invocations = 100) {
        checkAll(arbProtoNote) { protoNote ->
            val domain = NoteMapper.toDomain(protoNote)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Property 4: CreateNoteParams -> CreateNoteRequest preserves all fields").config(invocations = 100) {
        checkAll(arbCreateNoteParams) { params ->
            val proto = NoteMapper.toProto(params)
            proto.title shouldBe params.title
            proto.content shouldBe params.content
            proto.path shouldBe params.path
        }
    }

    test("Property 4: UpdateNoteParams -> UpdateNoteRequest preserves all fields").config(invocations = 100) {
        checkAll(arbUpdateNoteParams) { params ->
            val proto = NoteMapper.toProto(params)
            proto.file_path shouldBe params.filePath
            proto.content shouldBe params.content
        }
    }

    test("Property 4: CreateNoteResponse -> Domain Note preserves all fields").config(invocations = 100) {
        checkAll(arbProtoNote) { protoNote ->
            val response = notes.v1.CreateNoteResponse(
                file_path = protoNote.file_path,
                title = protoNote.title,
                content = protoNote.content,
                updated_at = protoNote.updated_at
            )
            val domain = NoteMapper.toDomain(response)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Property 4: GetNoteResponse -> Domain Note preserves all fields").config(invocations = 100) {
        checkAll(arbProtoNote) { protoNote ->
            val response = notes.v1.GetNoteResponse(
                file_path = protoNote.file_path,
                title = protoNote.title,
                content = protoNote.content,
                updated_at = protoNote.updated_at
            )
            val domain = NoteMapper.toDomain(response)
            domain.filePath shouldBe protoNote.file_path
            domain.title shouldBe protoNote.title
            domain.content shouldBe protoNote.content
            domain.updatedAt shouldBe protoNote.updated_at
        }
    }

    // -- Property 5: Timestamp Mapping Preservation --

    test("Property 5: Timestamp round-trip through Proto Note -> Domain preserves exact value").config(invocations = 100) {
        checkAll(arbTimestamp) { timestamp ->
            val protoNote = notes.v1.Note(
                file_path = "test.md",
                title = "test",
                content = "content",
                updated_at = timestamp
            )
            val domain = NoteMapper.toDomain(protoNote)
            domain.updatedAt shouldBe timestamp
        }
    }

    test("Property 5: Timestamp through CreateNoteResponse -> Domain preserves exact value").config(invocations = 100) {
        checkAll(arbTimestamp) { timestamp ->
            val response = notes.v1.CreateNoteResponse(
                file_path = "test.md",
                title = "test",
                content = "content",
                updated_at = timestamp
            )
            val domain = NoteMapper.toDomain(response)
            domain.updatedAt shouldBe timestamp
        }
    }

    test("Property 5: Timestamp through GetNoteResponse -> Domain preserves exact value").config(invocations = 100) {
        checkAll(arbTimestamp) { timestamp ->
            val response = notes.v1.GetNoteResponse(
                file_path = "test.md",
                title = "test",
                content = "content",
                updated_at = timestamp
            )
            val domain = NoteMapper.toDomain(response)
            domain.updatedAt shouldBe timestamp
        }
    }
})
