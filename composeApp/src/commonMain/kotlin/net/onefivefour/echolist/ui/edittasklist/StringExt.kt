package net.onefivefour.echolist.ui.edittasklist

internal fun String.singleLine() = this
    .trim()
    .replace("\r", "")
    .replace("\n", "")