package net.onefivefour.echolist.ui.editnote

import androidx.compose.ui.text.TextRange
import kotlin.math.max
import kotlin.math.min

enum class MarkdownToolbarAction(val label: String) {
    Bold("Bold"),
    BulletList("Bullets"),
    CheckboxUnchecked("Todo"),
    CheckboxChecked("Done"),
    Link("Link"),
    Heading1("H1"),
    Heading2("H2"),
    Heading3("H3")
}

data class MarkdownTextEdit(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int
)

internal object MarkdownFormatter {

    fun apply(
        action: MarkdownToolbarAction,
        text: String,
        selection: TextRange
    ): MarkdownTextEdit = when (action) {
        MarkdownToolbarAction.Bold -> applyBold(text, selection)
        MarkdownToolbarAction.BulletList -> prefixSelectedLines(text, selection, "- ")
        MarkdownToolbarAction.CheckboxUnchecked -> prefixSelectedLines(text, selection, "- [ ] ")
        MarkdownToolbarAction.CheckboxChecked -> prefixSelectedLines(text, selection, "- [x] ")
        MarkdownToolbarAction.Link -> applyLink(text, selection)
        MarkdownToolbarAction.Heading1 -> toggleHeading(text, selection, 1)
        MarkdownToolbarAction.Heading2 -> toggleHeading(text, selection, 2)
        MarkdownToolbarAction.Heading3 -> toggleHeading(text, selection, 3)
    }

    private fun applyBold(text: String, selection: TextRange): MarkdownTextEdit {
        val start = min(selection.start, selection.end)
        val end = max(selection.start, selection.end)
        return if (start == end) {
            val inserted = text.replaceRange(start, end, "****")
            MarkdownTextEdit(
                text = inserted,
                selectionStart = start + 2,
                selectionEnd = start + 2
            )
        } else {
            val selectedText = text.substring(start, end)
            val inserted = text.replaceRange(start, end, "**$selectedText**")
            MarkdownTextEdit(
                text = inserted,
                selectionStart = start + 2,
                selectionEnd = end + 2
            )
        }
    }

    private fun applyLink(text: String, selection: TextRange): MarkdownTextEdit {
        val start = min(selection.start, selection.end)
        val end = max(selection.start, selection.end)
        val selectedLabel = text.substring(start, end).ifBlank { "link text" }
        val urlPlaceholder = "https://example.com"
        val replacement = "[$selectedLabel]($urlPlaceholder)"
        val inserted = text.replaceRange(start, end, replacement)
        val urlStart = start + selectedLabel.length + 3
        return MarkdownTextEdit(
            text = inserted,
            selectionStart = if (start == end) start + 1 else urlStart,
            selectionEnd = if (start == end) start + 1 + selectedLabel.length else urlStart + urlPlaceholder.length
        )
    }

    private fun prefixSelectedLines(
        text: String,
        selection: TextRange,
        prefix: String
    ): MarkdownTextEdit {
        val lineRange = selectedLineRange(text, selection)
        val original = text.substring(lineRange.start, lineRange.endExclusive)
        val updated = original
            .split('\n')
            .joinToString("\n") { line -> prefix + line.removeMarkdownListPrefix() }
        val newText = text.replaceRange(lineRange.start, lineRange.endExclusive, updated)
        return MarkdownTextEdit(
            text = newText,
            selectionStart = lineRange.start,
            selectionEnd = lineRange.start + updated.length
        )
    }

    private fun toggleHeading(
        text: String,
        selection: TextRange,
        level: Int
    ): MarkdownTextEdit {
        val lineRange = currentLineRange(text, selection)
        val original = text.substring(lineRange.start, lineRange.endExclusive)
        val desiredPrefix = "#".repeat(level) + " "
        val existing = headingMatch.find(original)
        val updated = when {
            existing == null -> desiredPrefix + original
            existing.groupValues[1].length == level -> original.removePrefix(existing.value)
            else -> desiredPrefix + original.removePrefix(existing.value)
        }
        val newText = text.replaceRange(lineRange.start, lineRange.endExclusive, updated)
        return MarkdownTextEdit(
            text = newText,
            selectionStart = lineRange.start,
            selectionEnd = lineRange.start + updated.length
        )
    }

    private fun currentLineRange(text: String, selection: TextRange): TextSegment {
        val cursor = min(selection.start, selection.end)
        val lineStart = text.lastIndexOf('\n', max(cursor - 1, 0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
        return TextSegment(start = lineStart, endExclusive = lineEnd)
    }

    private fun selectedLineRange(text: String, selection: TextRange): TextSegment {
        if (text.isEmpty()) return TextSegment(start = 0, endExclusive = 0)

        val startAnchor = min(selection.start, selection.end)
        val endAnchor = if (selection.start == selection.end) {
            selection.end
        } else {
            max(selection.start, selection.end) - 1
        }.coerceAtLeast(0)

        val lineStart = text.lastIndexOf('\n', max(startAnchor - 1, 0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', endAnchor).let { if (it == -1) text.length else it }
        return TextSegment(start = lineStart, endExclusive = lineEnd)
    }

    private fun String.removeMarkdownListPrefix(): String = replaceFirst(listPrefixRegex, "")

    private val headingMatch = Regex("^(#{1,3})\\s+")
    private val listPrefixRegex = Regex("^- (\\[( |x|X)])? ?")
}

private data class TextSegment(
    val start: Int,
    val endExclusive: Int
)
