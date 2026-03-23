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
    }
}
