package net.onefivefour.echolist.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: proto-api-update
 * Property 6: Entry partitioning into folders and files
 * Property 7: File entry type classification and title derivation
 * Property 8: Folder name extraction from path
 *
 * Validates: Requirements 5.4, 5.5, 5.6, 5.7, 10.3, 10.4
 */
class EntryParsingPropertyTest : FunSpec({

    // -- Generators --

    val safeSegment = Arb.string(1..30).filter { !it.contains("/") && it.isNotBlank() }

    val arbFolderEntry = safeSegment.map { "$it/" }
    val arbNoteEntry = safeSegment.map { "note_$it" }
    val arbTaskEntry = safeSegment.map { "tasks_$it" }

    /** Generates a single entry that is randomly a folder, note, or task */
    val arbEntry = arbitrary {
        when (Arb.int(0..2).bind()) {
            0 -> arbFolderEntry.bind()
            1 -> arbNoteEntry.bind()
            else -> arbTaskEntry.bind()
        }
    }

    val arbMixedEntries = Arb.list(arbEntry, 0..20)

    // -- Property 6: Entry partitioning into folders and files --

    test("Feature: proto-api-update, Property 6: partitioning is exhaustive and mutually exclusive") {
        checkAll(PropTestConfig(iterations = 100), arbMixedEntries) { entries ->
            val folders = extractFolders(entries)
            val files = extractFiles(entries)

            // Every entry appears in exactly one partition
            folders.size + files.size shouldBe entries.size

            // Folder entries end with "/", file entries don't
            val folderEntries = entries.filter { it.endsWith("/") }
            val fileEntries = entries.filter { !it.endsWith("/") }
            folders shouldHaveSize folderEntries.size
            files shouldHaveSize fileEntries.size
        }
    }

    // -- Property 7: File entry type classification and title derivation --

    test("Feature: proto-api-update, Property 7: note entries get NOTE type and stripped title") {
        checkAll(PropTestConfig(iterations = 100), arbNoteEntry) { entry ->
            val files = extractFiles(listOf(entry))
            files shouldHaveSize 1
            val file = files.first()
            file.fileType shouldBe FileType.NOTE
            file.title shouldBe entry.removePrefix("note_")
        }
    }

    test("Feature: proto-api-update, Property 7: task entries get TASK_LIST type and stripped title") {
        checkAll(PropTestConfig(iterations = 100), arbTaskEntry) { entry ->
            val files = extractFiles(listOf(entry))
            files shouldHaveSize 1
            val file = files.first()
            file.fileType shouldBe FileType.TASK_LIST
            file.title shouldBe entry.removePrefix("tasks_")
        }
    }

    // -- Property 8: Folder name extraction from path --

    test("Feature: proto-api-update, Property 8: folder name is last segment before trailing slash") {
        checkAll(PropTestConfig(iterations = 100), arbFolderEntry) { entry ->
            val folders = extractFolders(listOf(entry))
            folders shouldHaveSize 1
            val folder = folders.first()
            val expectedName = entry.trimEnd('/').substringAfterLast('/')
            folder.name shouldBe expectedName
            folder.itemCount shouldBe 0
        }
    }

    test("Feature: proto-api-update, Property 8: nested folder path extracts correct name") {
        checkAll(PropTestConfig(iterations = 100), safeSegment, safeSegment) { parent, child ->
            val entry = "$parent/$child/"
            val folders = extractFolders(listOf(entry))
            folders shouldHaveSize 1
            folders.first().name shouldBe child
        }
    }
})
