package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: create-folder-dialog
 * Property 2: Confirm button enabled if and only if input is valid and not loading
 *
 * Validates: Requirements 1.4, 2.4, 3.2
 */
class CreateFolderUiStatePropertyTest : FunSpec({

    test("Feature: create-folder-dialog, Property 2: isConfirmEnabled iff folderName is non-blank after trim and not loading") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.string(0..50),
            Arb.boolean()
        ) { folderName, isLoading ->
            val state = CreateFolderUiState(
                isVisible = true,
                folderName = folderName,
                isLoading = isLoading
            )

            val expected = folderName.trim().isNotBlank() && !isLoading
            state.isConfirmEnabled shouldBe expected
        }
    }
})
