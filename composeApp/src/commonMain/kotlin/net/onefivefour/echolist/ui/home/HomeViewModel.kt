package net.onefivefour.echolist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.home_title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.domain.DirectoryChangeNotifier
import net.onefivefour.echolist.domain.repository.FileRepository
import org.jetbrains.compose.resources.getString

class HomeViewModel(
    private val path: String,
    private val fileRepository: FileRepository,
    private val directoryChangeNotifier: DirectoryChangeNotifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeScreenUiState(
            breadcrumbs = emptyList(),
            isLoading = true
        )
    )
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
        viewModelScope.launch {
            directoryChangeNotifier.directoryChanged.collect { changedPath ->
                if (changedPath == path) {
                    loadData()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadData()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun loadData() {
        val homeTitle = getString(Res.string.home_title)
        _uiState.update { current ->
            current.copy(
                breadcrumbs = buildBreadcrumbs(path, homeTitle)
            )
        }
        val result = fileRepository.listFiles(path)
        result.fold(
            onSuccess = { entries ->
                _uiState.update { current ->
                    current.copy(
                        breadcrumbs = buildBreadcrumbs(path, homeTitle),
                        fileEntries = entries,
                        isLoading = false,
                        error = null
                    )
                }
            },
            onFailure = { exception ->
                _uiState.update { current ->
                    current.copy(
                        breadcrumbs = buildBreadcrumbs(path, homeTitle),
                        fileEntries = emptyList(),
                        isLoading = false,
                        error = exception.message
                    )
                }
            }
        )
    }
}

internal fun buildBreadcrumbs(path: String, homeTitle: String): List<BreadcrumbItem> {
    val breadcrumbs = mutableListOf(BreadcrumbItem(label = homeTitle, path = ""))
    if (path.isEmpty()) return breadcrumbs

    val segments = path.trimStart('/').trimEnd('/').split('/')
    var accumulated = ""
    for (segment in segments) {
        accumulated = if (accumulated.isEmpty()) segment else "$accumulated/$segment"
        breadcrumbs.add(BreadcrumbItem(label = segment, path = accumulated))
    }
    return breadcrumbs
}