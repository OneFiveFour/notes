package net.onefivefour.echolist.ui.editnote

internal sealed interface MarkdownBlock {
    data class Paragraph(val content: List<MarkdownInline>) : MarkdownBlock
    data class Heading(val level: Int, val content: List<MarkdownInline>) : MarkdownBlock
    data class BulletList(val items: List<List<MarkdownInline>>) : MarkdownBlock
    data class Checklist(val items: List<ChecklistItem>) : MarkdownBlock
}

internal data class ChecklistItem(
    val isChecked: Boolean,
    val content: List<MarkdownInline>
)

internal sealed interface MarkdownInline {
    data class Text(val value: String) : MarkdownInline
    data class Bold(val content: List<MarkdownInline>) : MarkdownInline
    data class Link(val label: String, val url: String) : MarkdownInline
}

internal object MarkdownSubsetParser {

    fun parse(document: String): List<MarkdownBlock> {
        if (document.isBlank()) return emptyList()

        val blocks = mutableListOf<MarkdownBlock>()
        val paragraphLines = mutableListOf<String>()
        val lines = document.split('\n')
        var index = 0

        fun flushParagraph() {
            if (paragraphLines.isEmpty()) return
            blocks += MarkdownBlock.Paragraph(parseInline(paragraphLines.joinToString("\n")))
            paragraphLines.clear()
        }

        while (index < lines.size) {
            val line = lines[index]

            if (line.isBlank()) {
                flushParagraph()
                index++
                continue
            }

            headingRegex.matchEntire(line)?.let { match ->
                flushParagraph()
                blocks += MarkdownBlock.Heading(
                    level = match.groupValues[1].length,
                    content = parseInline(match.groupValues[2])
                )
                index++
                continue
            }

            checklistRegex.matchEntire(line)?.let {
                flushParagraph()
                val items = mutableListOf<ChecklistItem>()
                while (index < lines.size) {
                    val checklistMatch = checklistRegex.matchEntire(lines[index]) ?: break
                    items += ChecklistItem(
                        isChecked = checklistMatch.groupValues[1].equals("x", ignoreCase = true),
                        content = parseInline(checklistMatch.groupValues[2])
                    )
                    index++
                }
                blocks += MarkdownBlock.Checklist(items)
                continue
            }

            bulletRegex.matchEntire(line)?.let {
                flushParagraph()
                val items = mutableListOf<List<MarkdownInline>>()
                while (index < lines.size) {
                    if (checklistRegex.matches(lines[index])) break
                    val bulletMatch = bulletRegex.matchEntire(lines[index]) ?: break
                    items += parseInline(bulletMatch.groupValues[1])
                    index++
                }
                blocks += MarkdownBlock.BulletList(items)
                continue
            }

            paragraphLines += line
            index++
        }

        flushParagraph()
        return blocks
    }

    private fun parseInline(text: String, allowBold: Boolean = true): List<MarkdownInline> {
        if (text.isEmpty()) return emptyList()

        val parts = mutableListOf<MarkdownInline>()
        val plainText = StringBuilder()
        var index = 0

        fun flushText() {
            if (plainText.isNotEmpty()) {
                parts += MarkdownInline.Text(plainText.toString())
                plainText.clear()
            }
        }

        while (index < text.length) {
            if (allowBold && text.startsWith("**", index)) {
                val end = text.indexOf("**", index + 2)
                if (end > index + 2) {
                    flushText()
                    val inner = text.substring(index + 2, end)
                    parts += MarkdownInline.Bold(parseInline(inner, allowBold = false))
                    index = end + 2
                    continue
                }
            }

            if (text[index] == '[') {
                val labelEnd = text.indexOf(']', index + 1)
                val hasUrlStart = labelEnd != -1 && labelEnd + 1 < text.length && text[labelEnd + 1] == '('
                val urlEnd = if (hasUrlStart) text.indexOf(')', labelEnd + 2) else -1
                if (labelEnd > index && urlEnd > labelEnd + 2) {
                    flushText()
                    parts += MarkdownInline.Link(
                        label = text.substring(index + 1, labelEnd),
                        url = text.substring(labelEnd + 2, urlEnd)
                    )
                    index = urlEnd + 1
                    continue
                }
            }

            plainText.append(text[index])
            index++
        }

        flushText()
        return parts
    }

    private val headingRegex = Regex("^(#{1,3})\\s+(.+)$")
    private val checklistRegex = Regex("^- \\[( |x|X)] (.+)$")
    private val bulletRegex = Regex("^- (.+)$")
}
