package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.UpdateNoteParams
import net.onefivefour.echolist.data.source.cache.FakeCacheDataSource
import net.onefivefour.echolist.data.source.network.FakeNoteRemoteDataSource
import net.onefivefour.echolist.data.network.error.NetworkException
import notes.v1.CreateNoteResponse
import notes.v1.DeleteNoteResponse
import notes.v1.GetNoteResponse
import notes.v1.ListNotesResponse
import notes.v1.UpdateNoteResponse

class NotesRepositoryImplTest : FunSpec({

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

    // -- CreateNote --

    test("createNote returns mapped note on success").config(invocations = 20) {
        checkAll(arbCreateNoteParams, arbProtoNote) { params, protoNote ->
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

    test("createNote forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbCreateNoteParams) { params ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.createNoteResult = Result.success(
                CreateNoteResponse(
                    note = notes.v1.Note(
                        id = "new-uuid",
                        file_path = "/test.md",
                        title = "t",
                        content = "c",
                        updated_at = 0L
                    )
                )
            )
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.createNote(params)

            fakeNetwork.lastCreateRequest?.title shouldBe params.title
            fakeNetwork.lastCreateRequest?.content shouldBe params.content
            fakeNetwork.lastCreateRequest?.parent_dir shouldBe params.parentDir
        }
    }

    test("createNote returns failure when network throws") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.createNoteResult = Result.failure(NetworkException.ServerError(500, "boom"))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.createNote(CreateNoteParams("t", "c", "/dir"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ServerError>()
    }

    // -- ListNotes --

    test("listNotes returns mapped notes on success (no cache)") {
        val note1 = notes.v1.Note(id = "id-1", file_path = "/n1.md", title = "N1", content = "C1", updated_at = 100L)
        val note2 = notes.v1.Note(id = "id-2", file_path = "/n2.md", title = "N2", content = "C2", updated_at = 200L)
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.listNotesResult = Result.success(
            ListNotesResponse(
                notes = listOf(note1, note2)
            )
        )
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listNotes("/parent")

        result.isSuccess shouldBe true
        val notesList = result.getOrThrow()
        notesList.size shouldBe 2
        notesList[0].id shouldBe "id-1"
        notesList[0].filePath shouldBe "/n1.md"
        notesList[1].id shouldBe "id-2"
        notesList[1].filePath shouldBe "/n2.md"
    }

    test("listNotes forwards correct parent_dir to data source") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.listNotesResult = Result.success(ListNotesResponse())
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        repo.listNotes("/some/path")

        fakeNetwork.lastListRequest?.parent_dir shouldBe "/some/path"
    }

    test("listNotes returns empty result when response has no notes") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.listNotesResult = Result.success(ListNotesResponse(notes = emptyList()))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listNotes("")

        result.isSuccess shouldBe true
        result.getOrThrow() shouldBe emptyList()
    }

    test("listNotes returns failure when network throws and no cache") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.listNotesResult = Result.failure(NetworkException.TimeoutError("timed out"))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.listNotes("/any")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.TimeoutError>()
    }

    // -- GetNote --

    test("getNote returns mapped note on success (no cache)") {
        val protoNote = notes.v1.Note(
            id = "note-uuid-1",
            file_path = "/note.md",
            title = "Title",
            content = "Content",
            updated_at = 999L
        )
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.getNoteResult = Result.success(GetNoteResponse(note = protoNote))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.getNote("note-uuid-1")

        result.isSuccess shouldBe true
        val note = result.getOrThrow()
        note.id shouldBe "note-uuid-1"
        note.filePath shouldBe "/note.md"
        note.title shouldBe "Title"
        note.content shouldBe "Content"
        note.updatedAt shouldBe 999L
    }

    test("getNote forwards correct id to data source") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.getNoteResult = Result.success(
            GetNoteResponse(
                note = notes.v1.Note(id = "uuid-x", file_path = "/x.md", title = "t", content = "c", updated_at = 0L)
            )
        )
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        repo.getNote("uuid-x")

        fakeNetwork.lastGetRequest?.id shouldBe "uuid-x"
    }

    test("getNote returns failure when network throws and no cache") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.getNoteResult = Result.failure(NetworkException.ClientError(404, "not found"))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.getNote("missing-uuid")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- UpdateNote --

    test("updateNote returns mapped note on success").config(invocations = 20) {
        checkAll(arbUpdateNoteParams, arbProtoNote) { params, protoNote ->
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

    test("updateNote forwards correct proto fields to data source").config(invocations = 20) {
        checkAll(arbUpdateNoteParams) { params ->
            val fakeNetwork = FakeNoteRemoteDataSource()
            fakeNetwork.updateNoteResult = Result.success(
                UpdateNoteResponse(
                    note = notes.v1.Note(
                        id = params.id,
                        file_path = "/test.md",
                        title = "t",
                        content = "c",
                        updated_at = 0L
                    )
                )
            )
            val fakeCache = FakeCacheDataSource()
            val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

            repo.updateNote(params)

            fakeNetwork.lastUpdateRequest?.id shouldBe params.id
            fakeNetwork.lastUpdateRequest?.title shouldBe params.title
            fakeNetwork.lastUpdateRequest?.content shouldBe params.content
        }
    }

    test("updateNote returns failure when network throws") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.updateNoteResult = Result.failure(NetworkException.ClientError(400, "bad request"))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.updateNote(UpdateNoteParams("some-uuid", "t", "c"))

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.ClientError>()
    }

    // -- DeleteNote --

    test("deleteNote returns Unit on success") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.deleteNoteResult = Result.success(DeleteNoteResponse())
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.deleteNote("note-uuid-del")

        result.isSuccess shouldBe true
        result.getOrThrow() shouldBe Unit
    }

    test("deleteNote forwards correct id to data source") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.deleteNoteResult = Result.success(DeleteNoteResponse())
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        repo.deleteNote("target-uuid")

        fakeNetwork.lastDeleteRequest?.id shouldBe "target-uuid"
    }

    test("deleteNote returns failure when network throws") {
        val fakeNetwork = FakeNoteRemoteDataSource()
        fakeNetwork.deleteNoteResult = Result.failure(NetworkException.NetworkError("timeout"))
        val fakeCache = FakeCacheDataSource()
        val repo = NotesRepositoryImpl(fakeNetwork, fakeCache, FakeDirectoryChangeNotifier(), Dispatchers.Unconfined)

        val result = repo.deleteNote("some-uuid")

        result.isFailure shouldBe true
        result.exceptionOrNull().shouldBeInstanceOf<NetworkException.NetworkError>()
    }
})
