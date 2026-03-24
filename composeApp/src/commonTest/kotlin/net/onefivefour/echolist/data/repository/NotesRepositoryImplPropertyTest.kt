package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.UpdateNoteParams
import net.onefivefour.echolist.data.source.cache.FakeCacheDataSource
import net.onefivefour.echolist.data.source.network.FakeNoteRemoteDataSource
import notes.v1.CreateNoteResponse
import notes.v1.DeleteNoteResponse
import notes.v1.GetNoteResponse
import notes.v1.ListNotesResponse
import notes.v1.UpdateNoteResponse

/**
 * Feature: proto-api-update
 * Property 11: NotesRepository creates notes correctly
 * Property 12: NotesRepository lists notes correctly
 * Property 13: NotesRepository gets notes correctly
 * Property 14: NotesRepository updates notes correctly
 * Property 15: NotesRepository deletes notes correctly
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5
 */
class NotesRepositoryImplPropertyTest : FunSpec({

    // -- Generators --

    val arbProtoNote = arbitrary {
        notes.v1.Note(
            id = Arb.string(1..50).bind(),
            file_path = Arb.string(1..100).bind(),
            title = Arb.string(1..100).bind(),
            content = Arb.string(0..500).bind(),
            updated_at = Arb.long(0..Long.MAX_VALUE).bind()
        )
    }

    val arbCreateNoteParams = arbitrary {
        CreateNoteParams(
            title = Arb.string(1..100).bind(),
            content = Arb.string(0..500).bind(),
            parentDir = Arb.string(0..100).bind()
        )
    }

    val arbUpdateNoteParams = arbitrary {
        UpdateNoteParams(
            id = Arb.string(1..50).bind(),
            title = Arb.string(1..100).bind(),
            content = Arb.string(0..500).bind()
        )
    }

    // ---------------------------------------------------------------
    // Property 11: NotesRepository creates notes correctly
    // Validates: Requirements 6.1
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 11: NotesRepository creates notes correctly - returns mapped Note") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateNoteParams,
            arbProtoNote
        ) { params, protoNote ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.createNoteResult = Result.success(CreateNoteResponse(note = protoNote))
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.createNote(params)

            result.isSuccess shouldBe true
            val note = result.getOrThrow()
            note.id shouldBe protoNote.id
            note.filePath shouldBe protoNote.file_path
            note.title shouldBe protoNote.title
            note.content shouldBe protoNote.content
            note.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 11: NotesRepository creates notes correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbCreateNoteParams,
            arbProtoNote
        ) { params, protoNote ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.createNoteResult = Result.success(CreateNoteResponse(note = protoNote))
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.createNote(params)

            fakeNetwork.lastCreateRequest?.title shouldBe params.title
            fakeNetwork.lastCreateRequest?.content shouldBe params.content
            fakeNetwork.lastCreateRequest?.parent_dir shouldBe params.parentDir
        }
    }

    // ---------------------------------------------------------------
    // Property 12: NotesRepository lists notes correctly
    // Validates: Requirements 6.2
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 12: NotesRepository lists notes correctly - returns mapped notes") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100),
            Arb.list(arbProtoNote, 0..20)
        ) { parentDir, protoNotes ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.listNotesResult = Result.success(
                ListNotesResponse(notes = protoNotes)
            )
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.listNotes(parentDir)

            result.isSuccess shouldBe true
            val notesList = result.getOrThrow()
            notesList.size shouldBe protoNotes.size
            notesList.forEachIndexed { i, note ->
                note.id shouldBe protoNotes[i].id
                note.filePath shouldBe protoNotes[i].file_path
                note.title shouldBe protoNotes[i].title
                note.content shouldBe protoNotes[i].content
                note.updatedAt shouldBe protoNotes[i].updated_at
            }
        }
    }

    test("Feature: proto-api-update, Property 12: NotesRepository lists notes correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..100)
        ) { parentDir ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.listNotesResult = Result.success(ListNotesResponse())
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.listNotes(parentDir)

            fakeNetwork.lastListRequest?.parent_dir shouldBe parentDir
        }
    }

    // ---------------------------------------------------------------
    // Property 13: NotesRepository gets notes correctly
    // Validates: Requirements 6.3
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 13: NotesRepository gets notes correctly - returns mapped Note") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..50),
            arbProtoNote
        ) { noteId, protoNote ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.getNoteResult = Result.success(GetNoteResponse(note = protoNote))
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.getNote(noteId)

            result.isSuccess shouldBe true
            val note = result.getOrThrow()
            note.id shouldBe protoNote.id
            note.filePath shouldBe protoNote.file_path
            note.title shouldBe protoNote.title
            note.content shouldBe protoNote.content
            note.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 13: NotesRepository gets notes correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..50)
        ) { noteId ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.getNoteResult = Result.success(
                GetNoteResponse(
                    note = notes.v1.Note(
                        id = noteId,
                        file_path = "/some/path.md",
                        title = "t",
                        content = "c",
                        updated_at = 0L
                    )
                )
            )
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.getNote(noteId)

            fakeNetwork.lastGetRequest?.id shouldBe noteId
        }
    }

    // ---------------------------------------------------------------
    // Property 14: NotesRepository updates notes correctly
    // Validates: Requirements 6.4
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 14: NotesRepository updates notes correctly - returns mapped Note") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateNoteParams,
            arbProtoNote
        ) { params, protoNote ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.updateNoteResult = Result.success(UpdateNoteResponse(note = protoNote))
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.updateNote(params)

            result.isSuccess shouldBe true
            val note = result.getOrThrow()
            note.id shouldBe protoNote.id
            note.filePath shouldBe protoNote.file_path
            note.title shouldBe protoNote.title
            note.content shouldBe protoNote.content
            note.updatedAt shouldBe protoNote.updated_at
        }
    }

    test("Feature: proto-api-update, Property 14: NotesRepository updates notes correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            arbUpdateNoteParams,
            arbProtoNote
        ) { params, protoNote ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.updateNoteResult = Result.success(UpdateNoteResponse(note = protoNote))
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.updateNote(params)

            fakeNetwork.lastUpdateRequest?.id shouldBe params.id
            fakeNetwork.lastUpdateRequest?.title shouldBe params.title
            fakeNetwork.lastUpdateRequest?.content shouldBe params.content
        }
    }

    // ---------------------------------------------------------------
    // Property 15: NotesRepository deletes notes correctly
    // Validates: Requirements 6.5
    // ---------------------------------------------------------------

    test("Feature: proto-api-update, Property 15: NotesRepository deletes notes correctly - returns success") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100)
        ) { noteId ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.deleteNoteResult = Result.success(DeleteNoteResponse())
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            val result = repo.deleteNote(noteId)

            result.isSuccess shouldBe true
            result.getOrThrow() shouldBe Unit
        }
    }

    test("Feature: proto-api-update, Property 15: NotesRepository deletes notes correctly - maps request fields") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(1..100)
        ) { noteId ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.deleteNoteResult = Result.success(DeleteNoteResponse())
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.deleteNote(noteId)

            fakeNetwork.lastDeleteRequest?.id shouldBe noteId
        }
    }
})