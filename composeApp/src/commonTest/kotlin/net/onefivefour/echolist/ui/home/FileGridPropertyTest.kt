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
 * Feature: file-add-button, Property 1: AddFileButton is always the last item in the files section
 *
 * For any list of files (including empty), when the files section is built,
 * the AddButton should always appear as the final item after all File entries.
 * The number of items should equal files.size + 1.
 *
 * **Validates: Requirements 1.1**
 */
class FileGridPropertyTest : FunSpec({

    val arbFileUiModel = Arb.string(1..50).map { value ->
        FileUiModel(
            id = value,
            title = value,
            fileType = FileType.NOTE,
            preview = value,
            timestamp = value
        )
    }

    // Feature: file-add-button, Property 1: AddFileButton is always the last item in the files section
    // **Validates: Requirements 1.1**
    test("Property 1: AddFileButton is always the last item in the files section") {
        checkAll(
            PropTestConfig(iterations = 100),
            Arb.list(arbFileUiModel, 0..30)
        ) { files ->
            val gridItems = buildFileGridItems(files)

            gridItems.size shouldBe files.size + 1
            gridItems.last().shouldBeInstanceOf<FileGridCell.AddButton>()

            val fileItems = gridItems.dropLast(1)
            fileItems.forEachIndexed { index, cell ->
                cell.shouldBeInstanceOf<FileGridCell.File>()
                cell.file shouldBe files[index]
            }
        }
    }
})
