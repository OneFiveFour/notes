package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-based tests for the HomeScreen folder grid layout logic.
 *
 * Tests validate correctness properties from the design document
 * against the extracted [buildFolderGridItems] function.
 */
class HomeScreenGridPropertyTest : FunSpec({

    val arbFolderUiModel = Arb.string(1..20).map { name ->
        FolderUiModel(id = name, name = name, itemCount = 0)
    }

    // Feature: add-new-item-inline, Property 1: AddButton is always the last grid item
    // **Validates: Requirements 1.1**
    test("Property 1: AddButton is always the last grid item") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(arbFolderUiModel, 0..20)
        ) { folders ->
            val gridItems = buildFolderGridItems(folders, InlineCreationState.Hidden)

            gridItems.last().shouldBeInstanceOf<FolderGridCell.AddButton>()

            val folderItems = gridItems.dropLast(1)
            folderItems.forEachIndexed { index, cell ->
                cell.shouldBeInstanceOf<FolderGridCell.Folder>()
                cell.folder shouldBe folders[index]
            }
        }
    }

    // Feature: add-new-item-inline, Property 2: Grid rows are always padded to 2 columns
    // **Validates: Requirements 1.4**
    test("Property 2: Grid rows are always padded to 2 columns") {
        checkAll(
            PropTestConfig(iterations = 20),
            Arb.list(arbFolderUiModel, 0..20)
        ) { folders ->
            val gridItems = buildFolderGridItems(folders, InlineCreationState.Hidden)
            val totalItems = folders.size + 1 // folders + AddButton

            gridItems.size shouldBe totalItems

            val rows = gridItems.chunked(2)

            if (totalItems % 2 == 0) {
                // Even total: all rows have exactly 2 items
                rows.forEach { row ->
                    row.size shouldBe 2
                }
            } else {
                // Odd total: all rows except the last have 2 items, last row has 1
                rows.dropLast(1).forEach { row ->
                    row.size shouldBe 2
                }
                rows.last().size shouldBe 1
            }
        }
    }
})
