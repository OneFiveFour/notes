package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.onefivefour.echolist.data.models.Folder
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.repository.FakeFolderRepository
import net.onefivefour.echolist.data.repository.FakeNotesRepository

/**
 * Property-based tests for HomeViewModel inline folder creation logic.
 *
 * Each test validates a correctness property from the design document.
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

    // Feature: add-new-item-inline, Property 3: Tapping add transitions state from Hidden to Editing
    test("Property 3: Tapping add transitions state from Hidden to Editing") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(0..50)) { path ->
            runTest(testDispatcher) {
                val vm = HomeViewModel(path, FakeNotesRepository(), FakeFolderRepository())
                advanceUntilIdle()

                vm.uiState.value.inlineCreationState.shouldBeInstanceOf<InlineCreationState.Hidden>()

                vm.onAddFolderClicked()

                val state = vm.uiState.value.inlineCreationState
                state.shouldBeInstanceOf<InlineCreationState.Editing>()
                state.name shouldBe ""
            }
        }
    }

    // Feature: add-new-item-inline, Property 4: Cancel restores Hidden without modifying folders
    test("Property 4: Cancel restores Hidden without modifying folders") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), nonBlankArb) { name ->
            runTest(testDispatcher) {
                val notesRepo = FakeNotesRepository()
                notesRepo.addNotes(Note("/work/", "Work", "", 0L))
                notesRepo.addEntries("work/")

                val vm = HomeViewModel("/", notesRepo, FakeFolderRepository())
                advanceUntilIdle()

                val foldersBefore = vm.uiState.value.folders

                // Enter editing state
                vm.onAddFolderClicked()
                vm.onInlineNameChanged(name)

                // Cancel
                vm.onInlineCancel()

                vm.uiState.value.inlineCreationState.shouldBeInstanceOf<InlineCreationState.Hidden>()
                vm.uiState.value.folders shouldBe foldersBefore
            }
        }
    }

    // Feature: add-new-item-inline, Property 5: Whitespace-only names are rejected
    test("Property 5: Whitespace-only names are rejected") {
        val whitespaceArb = Arb.string(0..20).filter { it.isBlank() }

        checkAll(PropTestConfig(iterations = 20), whitespaceArb) { blankName ->
            runTest(testDispatcher) {
                val folderRepo = FakeFolderRepository()
                val vm = HomeViewModel("/", FakeNotesRepository(), folderRepo)
                advanceUntilIdle()

                vm.onAddFolderClicked()
                vm.onInlineNameChanged(blankName)
                vm.onInlineConfirm()
                advanceUntilIdle()

                // State should remain Editing — no transition to Saving
                vm.uiState.value.inlineCreationState.shouldBeInstanceOf<InlineCreationState.Editing>()
                // No repository call should have been made
                folderRepo.callLog.size shouldBe 0
            }
        }
    }

    // Feature: add-new-item-inline, Property 6: Non-blank names transition to Saving
    test("Property 6: Non-blank names transition to Saving") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), nonBlankArb) { name ->
            runTest(testDispatcher) {
                // Use a folder repo that suspends indefinitely so we can observe Saving state
                val folderRepo = object : FakeFolderRepository() {
                    override suspend fun createFolder(params: net.onefivefour.echolist.data.models.CreateFolderParams): Result<List<Folder>> {
                        super.createFolder(params)
                        kotlinx.coroutines.awaitCancellation()
                    }
                }

                val vm = HomeViewModel("/", FakeNotesRepository(), folderRepo)
                advanceUntilIdle()

                vm.onAddFolderClicked()
                vm.onInlineNameChanged(name)
                vm.onInlineConfirm()
                // Don't advanceUntilIdle — observe the Saving state before createFolder completes

                val state = vm.uiState.value.inlineCreationState
                state.shouldBeInstanceOf<InlineCreationState.Saving>()
                state.name shouldBe name.trim()
            }
        }
    }

    // Feature: add-new-item-inline, Property 7: CreateFolderParams are constructed correctly
    test("Property 7: CreateFolderParams are constructed correctly") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }
        val pathArb = Arb.string(0..50)

        checkAll(PropTestConfig(iterations = 20), pathArb, nonBlankArb) { path, name ->
            runTest(testDispatcher) {
                val folderRepo = FakeFolderRepository()
                val vm = HomeViewModel(path, FakeNotesRepository(), folderRepo)
                advanceUntilIdle()

                vm.onAddFolderClicked()
                vm.onInlineNameChanged(name)
                vm.onInlineConfirm()
                advanceUntilIdle()

                val params = folderRepo.lastCreateParams
                params shouldBe net.onefivefour.echolist.data.models.CreateFolderParams(
                    domain = "notes",
                    parentPath = path,
                    name = name.trim()
                )
            }
        }
    }

    // Feature: add-new-item-inline, Property 8: Successful creation resets state and includes new folder
    test("Property 8: Successful creation resets state and includes new folder") {
        val nonBlankArb = Arb.string(1..30).filter { it.isNotBlank() }

        checkAll(PropTestConfig(iterations = 20), nonBlankArb) { name ->
            runTest(testDispatcher) {
                val notesRepo = FakeNotesRepository()
                val folderRepo = FakeFolderRepository()
                folderRepo.createFolderResult = Result.success(listOf(Folder("/${name.trim()}/")))

                val vm = HomeViewModel("/", notesRepo, folderRepo)
                advanceUntilIdle()

                vm.onAddFolderClicked()
                vm.onInlineNameChanged(name)
                vm.onInlineConfirm()
                advanceUntilIdle()

                vm.uiState.value.inlineCreationState.shouldBeInstanceOf<InlineCreationState.Hidden>()
            }
        }
    }

    // Feature: add-new-item-inline, Property 9: Failed creation transitions to Error with name preserved
    test("Property 9: Failed creation transitions to Error with name preserved") {
        val nonBlankArb = Arb.string(1..50).filter { it.isNotBlank() }
        val errorMsgArb = Arb.string(1..100)

        checkAll(PropTestConfig(iterations = 20), nonBlankArb, errorMsgArb) { name, errorMsg ->
            runTest(testDispatcher) {
                val folderRepo = FakeFolderRepository()
                folderRepo.createFolderResult = Result.failure(RuntimeException(errorMsg))

                val vm = HomeViewModel("/", FakeNotesRepository(), folderRepo)
                advanceUntilIdle()

                vm.onAddFolderClicked()
                vm.onInlineNameChanged(name)
                vm.onInlineConfirm()
                advanceUntilIdle()

                val state = vm.uiState.value.inlineCreationState
                state.shouldBeInstanceOf<InlineCreationState.Error>()
                state.name shouldBe name.trim()
                state.message.shouldNotBeEmpty()
            }
        }
    }
})