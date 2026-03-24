package net.onefivefour.echolist.ui.editnote

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.dto.CreateNoteParams
import net.onefivefour.echolist.data.dto.UpdateNoteParams
import net.onefivefour.echolist.domain.model.Note
import net.onefivefour.echolist.domain.repository.NotesRepository

// Feature: note-tasklist-editors, Property 1: Save guard — repository called if and only if trimmed text is non-blank

@OptIn(ExperimentalCoroutinesApi::class, io.kotest.common.ExperimentalKotest::class)
class EditNoteViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // -- Fake --

    class FakeNotesRepository : NotesRepository {
        val createNoteCalls = mutableListOf<CreateNoteParams>()
        val getNoteCalls = mutableListOf<String>()
        val updateNoteCalls = mutableListOf<UpdateNoteParams>()
        private val notes = mutableMapOf<String, Note>()

        fun addNote(note: Note) {
            notes[note.id] = note
        }

        override suspend fun createNote(params: CreateNoteParams): Result<Note> {
            createNoteCalls.add(params)
            val note = Note(
                id = "generated-${params.title}",
                filePath = "${params.parentDir}/${params.title}",
                title = params.title,
                content = params.content,
                updatedAt = 0L
            )
            notes[note.id] = note
            return Result.success(note)
        }

        override suspend fun listNotes(parentDir: String): Result<List<Note>> =
            Result.success(emptyList())

        override suspend fun getNote(noteId: String): Result<Note> {
            getNoteCalls.add(noteId)
            return notes[noteId]?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Note not found: $noteId"))
        }

        override suspend fun updateNote(params: UpdateNoteParams): Result<Note> {
            updateNoteCalls.add(params)
            val existing = notes[params.id]
                ?: return Result.failure(NoSuchElementException("Note not found: ${params.id}"))
            val updated = existing.copy(content = params.content, updatedAt = existing.updatedAt + 1)
            notes[updated.id] = updated
            return Result.success(updated)
        }

        override suspend fun deleteNote(noteId: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    // -- Property 1: Save guard --

    test("Property 1: Save guard — repository called if and only if trimmed text is non-blank") {
        // Validates: Requirements 5.3, 5.7
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..50),
            Arb.string(0..100)
        ) { generatedTitle, generatedContent ->
            runTest(testDispatcher) {
                val fakeRepo = FakeNotesRepository()
                val parentPath = "test/path"
                val vm = EditNoteViewModel(
                    mode = EditNoteMode.Create(parentPath),
                    notesRepository = fakeRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                // Set the note content
                vm.uiState.value.contentState.edit {
                    replace(0, length, generatedContent)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                val trimmed = generatedTitle.trim()
                if (trimmed.isBlank()) {
                    fakeRepo.createNoteCalls.size shouldBe 0
                } else {
                    fakeRepo.createNoteCalls.size shouldBe 1
                    fakeRepo.createNoteCalls[0].title shouldBe trimmed
                    fakeRepo.createNoteCalls[0].content shouldBe generatedContent
                    fakeRepo.createNoteCalls[0].parentDir shouldBe parentPath
                }
            }
        }
    }

    // Feature: note-tasklist-editors, Property 2: Successful save emits navigate-back event

    test("Property 2: Successful save emits navigate-back event") {
        // Validates: Requirements 5.5
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..50).filter { it.isNotBlank() }) { generatedTitle ->
            runTest(testDispatcher) {
                val fakeRepo = FakeNotesRepository()
                val parentPath = "test/path"
                val vm = EditNoteViewModel(
                    mode = EditNoteMode.Create(parentPath),
                    notesRepository = fakeRepo
                )

                // Start collecting navigateBack before triggering save
                val navigateBackDeferred = async {
                    vm.navigateBack.first()
                }

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                // The deferred should complete with Unit, proving exactly one event was emitted
                navigateBackDeferred.await() shouldBe Unit
                vm.uiState.value.isSaving shouldBe false
            }
        }
    }

    // Feature: note-tasklist-editors, Property 3: Failed save sets error and clears loading

    test("Property 3: Failed save sets error and clears loading") {
        // Validates: Requirements 5.6
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..50).filter { it.isNotBlank() },
            Arb.string(1..100)
        ) { generatedTitle, errorMessage ->
            runTest(testDispatcher) {
                val failingRepo = object : NotesRepository {
                    override suspend fun createNote(params: CreateNoteParams): Result<Note> {
                        return Result.failure(RuntimeException(errorMessage))
                    }

                    override suspend fun listNotes(parentDir: String): Result<List<Note>> =
                        Result.success(emptyList())

                    override suspend fun getNote(noteId: String): Result<Note> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun deleteNote(noteId: String): Result<Unit> =
                        Result.failure(UnsupportedOperationException())
                }

                val parentPath = "test/path"
                val vm = EditNoteViewModel(
                    mode = EditNoteMode.Create(parentPath),
                    notesRepository = failingRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.isSaving shouldBe false
                vm.uiState.value.error shouldBe errorMessage
            }
        }
    }

    test("Property 4: Edit mode loads note data and treats empty content as loaded") {
        runTest(testDispatcher) {
            val fakeRepo = FakeNotesRepository()
            val note = Note(
                id = "note-empty-id",
                filePath = "note-empty.md",
                title = "note-empty",
                content = "",
                updatedAt = 1L
            )
            fakeRepo.addNote(note)

            val vm = EditNoteViewModel(
                mode = EditNoteMode.Edit(note.id),
                notesRepository = fakeRepo
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.mode shouldBe EditNoteMode.Edit(note.id)
            vm.uiState.value.isLoading shouldBe false
            vm.uiState.value.titleState.text.toString() shouldBe note.title
            vm.uiState.value.contentState.text.toString() shouldBe ""
            fakeRepo.getNoteCalls shouldBe listOf(note.id)
        }
    }

    test("Property 5: Edit mode save updates the existing note") {
        runTest(testDispatcher) {
            val fakeRepo = FakeNotesRepository()
            val note = Note(
                id = "note-id",
                filePath = "note.md",
                title = "note",
                content = "before",
                updatedAt = 1L
            )
            fakeRepo.addNote(note)

            val vm = EditNoteViewModel(
                mode = EditNoteMode.Edit(note.id),
                notesRepository = fakeRepo
            )

            testScheduler.advanceUntilIdle()

            vm.uiState.value.contentState.edit {
                replace(0, length, "after")
            }

            vm.onSaveClick()
            testScheduler.advanceUntilIdle()

            fakeRepo.createNoteCalls shouldBe emptyList()
            fakeRepo.updateNoteCalls shouldBe listOf(
                UpdateNoteParams(
                    id = note.id,
                    content = "after"
                )
            )
            vm.uiState.value.isSaving shouldBe false
        }
    }
})
