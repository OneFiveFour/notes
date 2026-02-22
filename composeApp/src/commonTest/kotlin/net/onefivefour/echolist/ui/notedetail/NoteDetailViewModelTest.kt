package net.onefivefour.echolist.ui.notedetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.repository.FakeNotesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emitsSuccessForValidNoteId() = runTest(testDispatcher) {
        val repo = FakeNotesRepository()
        repo.addNotes(Note("docs/hello.md", "Hello", "Hello world", 60_000L))

        val viewModel = NoteDetailViewModel("docs/hello.md", repo)
        advanceUntilIdle()

        val state = assertIs<NoteDetailUiState.Success>(viewModel.uiState.value)
        assertEquals("Hello", state.title)
        assertEquals("Hello world", state.content)
    }

    @Test
    fun emitsErrorForInvalidNoteId() = runTest(testDispatcher) {
        val repo = FakeNotesRepository()

        val viewModel = NoteDetailViewModel("nonexistent", repo)
        advanceUntilIdle()

        val state = assertIs<NoteDetailUiState.Error>(viewModel.uiState.value)
        assertEquals(true, state.message.isNotEmpty())
    }
}
