package net.onefivefour.echolist.data.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PathUtilsTest : FunSpec({

    test("normalizePath collapses repeated leading slashes to a single root slash") {
        normalizePath("//note_MyNote.md") shouldBe "/note_MyNote.md"
    }

    test("normalizePath leaves non-root internal slashes untouched") {
        normalizePath("folder//note_MyNote.md") shouldBe "folder//note_MyNote.md"
    }

    test("joinPath uses a single slash for root parent paths") {
        joinPath("/", "note_MyNote.md") shouldBe "/note_MyNote.md"
    }
})
