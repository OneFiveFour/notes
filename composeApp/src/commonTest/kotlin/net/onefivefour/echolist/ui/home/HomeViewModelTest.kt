package net.onefivefour.echolist.ui.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.repository.FakeFileRepository
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
    fun loadDataCallsFileRepositoryListFiles() = runTest(testDispatcher) {
        val repo = FakeFileRepository()
        repo.listFilesResult = Result.success(listOf("work/", "note_meeting-notes", "tasks_shopping"))

        val viewModel = HomeViewModel("/", repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Home", state.title)
        assertEquals(1, state.breadcrumbs.size)
        assertEquals(1, state.folders.size)
        assertEquals("work", state.folders[0].name)
        assertEquals(0, state.folders[0].itemCount)
        assertEquals(2, state.files.size)
        assertEquals("meeting-notes", state.files[0].title)
        assertEquals(FileType.NOTE, state.files[0].fileType)
        assertEquals("shopping", state.files[1].title)
        assertEquals(FileType.TASK_LIST, state.files[1].fileType)
        // Verify listFiles was called
        assertEquals(1, repo.callLog.count { it.startsWith("listFiles") })
    }

    @Test
    fun folderCreationTriggersReloadViaListFiles() = runTest(testDispatcher) {
        val repo = FakeFileRepository()
        repo.listFilesResult = Result.success(emptyList())

        val viewModel = HomeViewModel("/", repo)
        advanceUntilIdle()

        // Initial load = 1 listFiles call
        assertEquals(1, repo.callLog.count { it.startsWith("listFiles") })

        viewModel.onAddFolderClicked()
        viewModel.onInlineNameChanged("New Folder")
        viewModel.onInlineConfirm()
        advanceUntilIdle()

        // After creation: createFolder + reload listFiles = 2 total listFiles calls
        assertEquals(1, repo.callLog.count { it.startsWith("createFolder") })
        assertEquals(2, repo.callLog.count { it.startsWith("listFiles") })
    }

    @Test
    fun emitsEmptyListsOnRepositoryFailure() = runTest(testDispatcher) {
        val repo = FakeFileRepository()
        repo.listFilesResult = Result.failure(RuntimeException("network error"))

        val viewModel = HomeViewModel("/docs", repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("docs", state.title)
        assertEquals(emptyList(), state.folders)
        assertEquals(emptyList(), state.files)
    }
}
