package net.onefivefour.echolist.ui.editnote

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class MarkdownSubsetParserTest : FunSpec({

    test("parses headings, bullets, checklists, and paragraphs") {
        val blocks = MarkdownSubsetParser.parse(
            """
            # Heading

            Intro with **bold** and [Docs](https://example.com)
            - first
            - second
            - [ ] third
            """.trimIndent()
        )

        blocks[0].shouldBeInstanceOf<MarkdownBlock.Heading>().level shouldBe 1
        blocks[1].shouldBeInstanceOf<MarkdownBlock.Paragraph>()
        blocks[2].shouldBeInstanceOf<MarkdownBlock.BulletList>()
        blocks[3].shouldBeInstanceOf<MarkdownBlock.Checklist>()
    }

    test("unsupported markdown falls back to paragraph text") {
        val blocks = MarkdownSubsetParser.parse("*italic*")

        blocks.size shouldBe 1
        val paragraph = blocks.single().shouldBeInstanceOf<MarkdownBlock.Paragraph>()
        paragraph.content.single().shouldBeInstanceOf<MarkdownInline.Text>().value shouldBe "*italic*"
    }

    test("blank lines split paragraphs") {
        val blocks = MarkdownSubsetParser.parse("first\n\nsecond")

        blocks.size shouldBe 2
        blocks[0].shouldBeInstanceOf<MarkdownBlock.Paragraph>()
        blocks[1].shouldBeInstanceOf<MarkdownBlock.Paragraph>()
    }
})
