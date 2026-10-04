package net.onefivefour.echolist.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.onefivefour.echolist.domain.DirectoryChangeNotifier

class FakeDirectoryChangeNotifier : DirectoryChangeNotifier {
    private val _directoryChanged = MutableSharedFlow<String>()
    override val directoryChanged: SharedFlow<String> = _directoryChanged.asSharedFlow()

    val emissions = mutableListOf<String>()

    override suspend fun notifyChanged(path: String) {
        emissions.add(path)
        _directoryChanged.emit(path)
    }
}
