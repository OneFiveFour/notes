package net.onefivefour.echolist.feature.note.data.source.cache

import kotlin.time.Clock

internal actual fun currentEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
