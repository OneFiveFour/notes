package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PathUtilsTest : FunSpec({

    test("normalizePath trims leading slashes so repository paths stay relative") {
        normalizePath("//note_MyNote.md") shouldBe "note_MyNote.md"
    }

    test("normalizePath collapses repeated internal slashes to a single separator") {
        normalizePath("folder//note_MyNote.md") shouldBe "folder/note_MyNote.md"
    }

    test("joinPath treats the root parent path as an empty relative base") {
        joinPath("/", "note_MyNote.md") shouldBe "note_MyNote.md"
    }
})
