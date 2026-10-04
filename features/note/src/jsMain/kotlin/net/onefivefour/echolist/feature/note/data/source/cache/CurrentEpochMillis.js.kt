package net.onefivefour.echolist.feature.note.data.source.cache

import kotlin.js.Date

internal actual fun currentEpochMillis(): Long = Date.now().toLong()