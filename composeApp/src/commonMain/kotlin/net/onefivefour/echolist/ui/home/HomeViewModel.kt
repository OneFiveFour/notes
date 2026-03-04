package net.onefivefour.echolist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.repository.FileRepository
import kotlin.collections.emptyList

class HomeViewModel(
    private val path: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeScreenUiState(
            title = "",
            breadcrumbs = emptyList()
        )
    )
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        val result = fileRepository.listFiles(path)
        result.fold(
            onSuccess = { entries ->
                _uiState.update { current ->
                    current.copy(
                        title = titleFromPath(path),
                        breadcrumbs = buildBreadcrumbs(path)
                    )
                }
            },
            onFailure = {
                _uiState.update { current ->
                    current.copy(
                        title = titleFromPath(path),
                        breadcrumbs = buildBreadcrumbs(path)
                    )
                }
            }
        )
    }
}

internal fun titleFromPath(path: String): String {
    if (path == "/" || path.isEmpty()) return "Home"
    return path.trimEnd('/').substringAfterLast('/')
}

internal fun buildBreadcrumbs(path: String): List<BreadcrumbItem> {
    val breadcrumbs = mutableListOf(BreadcrumbItem(label = "Home", path = "/"))
    if (path == "/" || path.isEmpty()) return breadcrumbs

    val segments = path.trimStart('/').trimEnd('/').split('/')
    var accumulated = ""
    for (segment in segments) {
        accumulated = "$accumulated/$segment"
        breadcrumbs.add(BreadcrumbItem(label = segment, path = accumulated))
    }
    return breadcrumbs
}
