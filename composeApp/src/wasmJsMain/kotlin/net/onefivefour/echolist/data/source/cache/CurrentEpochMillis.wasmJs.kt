package net.onefivefour.echolist.data.source.cache

import kotlin.js.Date

internal actual fun currentEpochMillis(): Long = Date.now().toLong()
