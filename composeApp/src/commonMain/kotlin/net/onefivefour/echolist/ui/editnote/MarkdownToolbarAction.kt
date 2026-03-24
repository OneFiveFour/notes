package net.onefivefour.echolist.ui.editnote

import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.format_check_box_checked
import echolist.composeapp.generated.resources.format_check_box_unchecked
import echolist.composeapp.generated.resources.format_bold
import echolist.composeapp.generated.resources.format_h1
import echolist.composeapp.generated.resources.format_h2
import echolist.composeapp.generated.resources.format_h3
import echolist.composeapp.generated.resources.format_list_bulleted
import echolist.composeapp.generated.resources.link
import org.jetbrains.compose.resources.DrawableResource

enum class MarkdownToolbarAction(
    val label: String,
    val iconRes: DrawableResource
) {
    Bold("Bold", Res.drawable.format_bold),
    BulletList("Bullets", Res.drawable.format_list_bulleted),
    CheckboxUnchecked("Todo", Res.drawable.format_check_box_unchecked),
    CheckboxChecked("Done", Res.drawable.format_check_box_checked),
    Link("Link", Res.drawable.link),
    Heading1("H1", Res.drawable.format_h1),
    Heading2("H2", Res.drawable.format_h2),
    Heading3("H3", Res.drawable.format_h3)
}