package net.onefivefour.echolist.ui.home

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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun emitsCorrectStateForRootPath() = runTest(testDispatcher) {
        val repo = FakeNotesRepository()
        repo.addNotes(
            Note("/notes/hello.md", "Hello", "Hello content", 60_000L),
            Note("/readme.md", "Readme", "Readme content", 120_000L)
        )

        val viewModel = HomeViewModel("/", repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Home", state.title)
        assertEquals(1, state.breadcrumbs.size)
        assertEquals("Home", state.breadcrumbs[0].label)
        // "/notes/hello.md" is under subfolder "notes" → 1 folder
        assertEquals(1, state.folders.size)
        assertEquals("notes", state.folders[0].name)
        // "/readme.md" is a direct child → 1 file
        assertEquals(1, state.files.size)
        assertEquals("Readme", state.files[0].title)
    }

    @Test
    fun emitsEmptyListsOnRepositoryFailure() = runTest(testDispatcher) {
        val repo = FakeNotesRepository()
        repo.setShouldFail(RuntimeException("network error"))

        val viewModel = HomeViewModel("/docs", repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("docs", state.title)
        assertEquals(emptyList(), state.folders)
        assertEquals(emptyList(), state.files)
    }
}
