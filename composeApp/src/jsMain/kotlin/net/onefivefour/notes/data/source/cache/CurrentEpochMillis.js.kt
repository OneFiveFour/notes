package net.onefivefour.notes.data.source.cache

import kotlin.js.Date

internal actual fun currentEpochMillis(): Long = Date.now().toLong()
