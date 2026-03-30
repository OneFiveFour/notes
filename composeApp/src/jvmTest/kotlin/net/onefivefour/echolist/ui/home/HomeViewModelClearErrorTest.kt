package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.FakeDirectoryChangeNotifier
import net.onefivefour.echolist.data.dto.CreateFolderParams
import net.onefivefour.echolist.data.dto.DeleteFolderParams
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.domain.repository.FileRepository

/**
 * Tests for [HomeViewModel.clearErrorAndReload].
 *
 * Validates that stale error state from a previous session (e.g. expired auth token)
 * is cleared when the composable re-enters composition after re-authentication.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelClearErrorTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("clearErrorAndReload clears a stale error and reloads successfully") {
        runTest(testDispatcher) {
            var shouldFail = true
            val repo = object : FileRepository {
                override suspend fun listFiles(parentPath: String): Result<List<FileEntry>> {
                    return if (shouldFail) {
                        Result.failure(Exception("code Unauthenticated token expired"))
                    } else {
                        Result.success(emptyList())
                    }
                }
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(UnsupportedOperationException())
            }

            val vm = HomeViewModel(
                path = "",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )

            // Let init's loadData() complete — it should fail
            advanceUntilIdle()
            vm.uiState.value.error.shouldNotBeNull()
            vm.uiState.value.error shouldBe "code Unauthenticated token expired"

            // Simulate successful re-authentication: network calls now succeed
            shouldFail = false

            // Simulate composable re-entering composition after re-auth
            vm.clearErrorAndReload()
            advanceUntilIdle()

            vm.uiState.value.error.shouldBeNull()
            vm.uiState.value.isLoading shouldBe false
        }
    }

    test("clearErrorAndReload reloads data even when previous load failed") {
        runTest(testDispatcher) {
            var callCount = 0
            val repo = object : FileRepository {
                override suspend fun listFiles(parentPath: String): Result<List<FileEntry>> {
                    callCount++
                    return Result.failure(Exception("token expired"))
                }
                override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
                    Result.failure(UnsupportedOperationException())
                override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
                    Result.failure(UnsupportedOperationException())
            }

            val vm = HomeViewModel(
                path = "",
                fileRepository = repo,
                directoryChangeNotifier = FakeDirectoryChangeNotifier()
            )

            // Let init's loadData() complete
            advanceUntilIdle()
            val callsAfterInit = callCount
            callsAfterInit shouldBe 1

            // clearErrorAndReload should trigger another listFiles call
            vm.clearErrorAndReload()
            advanceUntilIdle()

            callCount shouldBe callsAfterInit + 1
        }
    }
})
