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
import net.onefivefour.echolist.data.models.CreateNoteParams
import net.onefivefour.echolist.data.models.ListNotesResult
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.models.UpdateNoteParams
import net.onefivefour.echolist.data.repository.NotesRepository

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

        override suspend fun createNote(params: CreateNoteParams): Result<Note> {
            createNoteCalls.add(params)
            return Result.success(
                Note(
                    filePath = "${params.parentDir}/${params.title}",
                    title = params.title,
                    content = params.content,
                    updatedAt = 0L
                )
            )
        }

        override suspend fun listNotes(parentDir: String): Result<ListNotesResult> =
            Result.success(ListNotesResult(notes = emptyList(), entries = emptyList()))

        override suspend fun getNote(filePath: String): Result<Note> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateNote(params: UpdateNoteParams): Result<Note> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteNote(filePath: String): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    // -- Property 1: Save guard --

    test("Property 1: Save guard — repository called if and only if trimmed text is non-blank") {
        // Validates: Requirements 5.3, 5.7
        checkAll(PropTestConfig(iterations = 100), Arb.string(0..50)) { generatedString ->
            runTest(testDispatcher) {
                val fakeRepo = FakeNotesRepository()
                val parentPath = "/test/path"
                val vm = EditNoteViewModel(
                    parentPath = parentPath,
                    notesRepository = fakeRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedString)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                val trimmed = generatedString.trim()
                if (trimmed.isBlank()) {
                    fakeRepo.createNoteCalls.size shouldBe 0
                } else {
                    fakeRepo.createNoteCalls.size shouldBe 1
                    fakeRepo.createNoteCalls[0].title shouldBe trimmed
                    fakeRepo.createNoteCalls[0].content shouldBe ""
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
                val parentPath = "/test/path"
                val vm = EditNoteViewModel(
                    parentPath = parentPath,
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

                    override suspend fun listNotes(parentDir: String): Result<ListNotesResult> =
                        Result.success(ListNotesResult(notes = emptyList(), entries = emptyList()))

                    override suspend fun getNote(filePath: String): Result<Note> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun updateNote(params: UpdateNoteParams): Result<Note> =
                        Result.failure(UnsupportedOperationException())

                    override suspend fun deleteNote(filePath: String): Result<Unit> =
                        Result.failure(UnsupportedOperationException())
                }

                val parentPath = "/test/path"
                val vm = EditNoteViewModel(
                    parentPath = parentPath,
                    notesRepository = failingRepo
                )

                // Set the title text
                vm.uiState.value.titleState.edit {
                    replace(0, length, generatedTitle)
                }

                vm.onSaveClick()
                testScheduler.advanceUntilIdle()

                vm.uiState.value.isLoading shouldBe false
                vm.uiState.value.error shouldBe errorMessage
            }
        }
    }
})
