package net.onefivefour.echolist.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.onefivefour.echolist.domain.DirectoryChangeNotifier

internal class DirectoryChangeNotifierImpl : DirectoryChangeNotifier {
    private val _directoryChanged = MutableSharedFlow<String>()
    override val directoryChanged: SharedFlow<String> = _directoryChanged.asSharedFlow()

    override suspend fun notifyChanged(path: String) {
        _directoryChanged.emit(path)
        // Also notify all ancestor directories so parent ViewModels refresh
        var remaining = path
        while (remaining.isNotEmpty()) {
            val lastSep = remaining.lastIndexOf('/')
            remaining = if (lastSep >= 0) remaining.substring(0, lastSep) else ""
            _directoryChanged.emit(remaining)
        }
    }
}
