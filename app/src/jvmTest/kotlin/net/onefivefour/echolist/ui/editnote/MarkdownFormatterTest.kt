package net.onefivefour.echolist.ui.editnote

import androidx.compose.ui.text.TextRange
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MarkdownFormatterTest : FunSpec({

    test("bold wraps the selected text") {
        val result = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.Bold,
            text = "hello world",
            selection = TextRange(6, 11)
        )

        result.text shouldBe "hello **world**"
        result.selectionStart shouldBe 8
        result.selectionEnd shouldBe 13
    }

    test("bullets prefix each selected line") {
        val result = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.BulletList,
            text = "first\nsecond",
            selection = TextRange(0, 12)
        )

        result.text shouldBe "- first\n- second"
    }

    test("checkbox action rewrites existing list prefixes") {
        val result = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.CheckboxUnchecked,
            text = "- done",
            selection = TextRange(0, 6)
        )

        result.text shouldBe "- [ ] done"
    }

    test("link action inserts a markdown template") {
        val result = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.Link,
            text = "Docs",
            selection = TextRange(0, 4)
        )

        result.text shouldBe "[Docs](https://example.com)"
        result.selectionStart shouldBe 7
        result.selectionEnd shouldBe 26
    }

    test("heading action toggles the current line") {
        val added = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.Heading2,
            text = "Title",
            selection = TextRange(3)
        )
        added.text shouldBe "## Title"

        val removed = MarkdownFormatter.apply(
            action = MarkdownToolbarAction.Heading2,
            text = added.text,
            selection = TextRange(3)
        )
        removed.text shouldBe "Title"
    }
})
