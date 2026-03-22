package net.onefivefour.echolist.ui.editnote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import net.onefivefour.echolist.ui.theme.EchoListTheme

@Composable
internal fun MarkdownPreview(
    document: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val blocks = remember(document) { MarkdownSubsetParser.parse(document) }
    val linkColor = EchoListTheme.materialColors.primary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.m)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> MarkdownText(
                    annotatedString = block.content.toAnnotatedString(linkColor),
                    style = when (block.level) {
                        1 -> EchoListTheme.typography.titleLarge
                        2 -> EchoListTheme.typography.titleMedium
                        else -> EchoListTheme.typography.titleSmall
                    },
                    onOpenUrl = uriHandler::openUri
                )

                is MarkdownBlock.Paragraph -> MarkdownText(
                    annotatedString = block.content.toAnnotatedString(linkColor),
                    style = EchoListTheme.typography.bodyMedium,
                    onOpenUrl = uriHandler::openUri
                )

                is MarkdownBlock.BulletList -> Column(
                    verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    block.items.forEach { item ->
                        MarkdownListRow(
                            prefix = "-",
                            content = item.toAnnotatedString(linkColor),
                            onOpenUrl = uriHandler::openUri
                        )
                    }
                }

                is MarkdownBlock.Checklist -> Column(
                    verticalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.xs)
                ) {
                    block.items.forEach { item ->
                        MarkdownListRow(
                            prefix = if (item.isChecked) "[x]" else "[ ]",
                            content = item.content.toAnnotatedString(linkColor),
                            onOpenUrl = uriHandler::openUri
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownListRow(
    prefix: String,
    content: AnnotatedString,
    onOpenUrl: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EchoListTheme.dimensions.s)
    ) {
        Text(
            text = prefix,
            style = EchoListTheme.typography.bodyMedium,
            color = EchoListTheme.materialColors.onSurface
        )
        MarkdownText(
            annotatedString = content,
            style = EchoListTheme.typography.bodyMedium,
            onOpenUrl = onOpenUrl,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownText(
    annotatedString: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLinks = annotatedString.getStringAnnotations(URL_TAG, 0, annotatedString.length).isNotEmpty()
    if (hasLinks) {
        ClickableText(
            text = annotatedString,
            style = style.copy(color = EchoListTheme.materialColors.onSurface),
            modifier = modifier,
            onClick = { offset ->
                annotatedString
                    .getStringAnnotations(URL_TAG, offset, offset)
                    .firstOrNull()
                    ?.let { onOpenUrl(it.item) }
            }
        )
    } else {
        Text(
            text = annotatedString,
            style = style,
            color = EchoListTheme.materialColors.onSurface,
            modifier = modifier
        )
    }
}

private fun List<MarkdownInline>.toAnnotatedString(linkColor: Color): AnnotatedString = buildAnnotatedString {
    appendInlineContent(this@toAnnotatedString, linkColor)
}

private fun AnnotatedString.Builder.appendInlineContent(
    inlines: List<MarkdownInline>,
    linkColor: Color
) {
    inlines.forEach { inline ->
        when (inline) {
            is MarkdownInline.Text -> append(inline.value)
            is MarkdownInline.Bold -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendInlineContent(inline.content, linkColor)
                pop()
            }

            is MarkdownInline.Link -> {
                pushStringAnnotation(URL_TAG, inline.url)
                pushStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                )
                append(inline.label)
                pop()
                pop()
            }
        }
    }
}

private const val URL_TAG = "url"
