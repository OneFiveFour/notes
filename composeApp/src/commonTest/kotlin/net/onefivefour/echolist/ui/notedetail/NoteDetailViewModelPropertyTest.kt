package net.onefivefour.echolist.ui.notedetail

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
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
import net.onefivefour.echolist.data.repository.FakeNotesRepository

/**
 * Feature: compose-navigation-3, Property 5: Missing note produces error state
 *
 * For any noteId that does not exist in the repository, the NoteDetailViewModel
 * should produce a NoteDetailUiState.Error state with a non-empty error message.
 *
 * **Validates: Requirements 6.3**
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelPropertyTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    test("Property 5: Missing note produces error state") {
        checkAll(PropTestConfig(iterations = 20), Arb.string(0..200)) { noteId ->
            runTest(testDispatcher) {
                val repository = FakeNotesRepository()
                // Repository is empty — any noteId will fail
                val viewModel = NoteDetailViewModel(noteId, repository)
                advanceUntilIdle()

                val state = viewModel.uiState.value
                state.shouldBeInstanceOf<NoteDetailUiState.Error>()
                state.message.shouldNotBeEmpty()
            }
        }
    }
})
