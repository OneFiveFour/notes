package net.onefivefour.echolist.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class DirectoryChangeNotifier {

    private val _changes = MutableSharedFlow<String>()
    val changes: SharedFlow<String> = _changes.asSharedFlow()

    suspend fun notify(path: String) {
        _changes.emit(path)
    }
}

internal fun parentDirectoryOf(filePath: String): String {
    val lastSlash = filePath.lastIndexOf('/')
    return when {
        lastSlash <= 0 -> "/"
        else -> filePath.substring(0, lastSlash)
    }
}
