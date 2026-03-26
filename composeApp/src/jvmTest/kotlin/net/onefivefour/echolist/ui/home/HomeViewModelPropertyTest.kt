package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
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
import net.onefivefour.echolist.data.models.FileEntry
import net.onefivefour.echolist.domain.model.Folder
import net.onefivefour.echolist.data.dto.UpdateFolderParams
import net.onefivefour.echolist.domain.repository.FileRepository
import java.util.concurrent.atomic.AtomicInteger

/**
 * Property-based tests for HomeViewModel directoryChanged observation.
 *
 * Feature: create-folder-dialog, Property 7: HomeViewModel refreshes on matching directoryChanged signal
 *
 * Validates: Requirements 2.2, 4.5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // -- Generators --

    val arbPath: Arb<String> = Arb.string(1..50)

    // -- Mock --

    class MockFileRepository : FileRepository {
        val listFilesCallCount = AtomicInteger(0)

        override suspend fun listFiles(parentPath: String): Result<List<FileEntry>> {
            listFilesCallCount.incrementAndGet()
            return Result.success(emptyList())
        }

        override suspend fun createFolder(params: CreateFolderParams): Result<Folder> =
            Result.failure(UnsupportedOperationException())

        override suspend fun updateFolder(params: UpdateFolderParams): Result<Folder> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteFolder(params: DeleteFolderParams): Result<Unit> =
            Result.failure(UnsupportedOperationException())
    }

    // -- Property 7: HomeViewModel refreshes on matching directoryChanged signal --

    test("Feature: create-folder-dialog, Property 7: HomeViewModel refreshes on matching directoryChanged signal") {
        checkAll(PropTestConfig(iterations = 100), arbPath, arbPath) { vmPath, emittedPath ->
            runTest(testDispatcher) {
                val repo = MockFileRepository()
                val notifier = FakeDirectoryChangeNotifier()
                val vm = HomeViewModel(path = vmPath, fileRepository = repo, directoryChangeNotifier = notifier)

                // Let init's loadData() complete
                advanceUntilIdle()
                val callsAfterInit = repo.listFilesCallCount.get()

                // Emit a path on directoryChanged
                notifier.notifyChanged(emittedPath)
                advanceUntilIdle()

                val callsAfterEmit = repo.listFilesCallCount.get()

                if (emittedPath == vmPath) {
                    // Matching path: listFiles should have been called again
                    callsAfterEmit shouldBe callsAfterInit + 1
                } else {
                    // Non-matching path: no additional listFiles call
                    callsAfterEmit shouldBe callsAfterInit
                }
            }
        }
    }
})
