package net.onefivefour.echolist.feature.tasklist.ui

internal fun String.singleLine() = this
    .trim()
    .replace("\r", "")
    .replace("\n", "")