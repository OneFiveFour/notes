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

    /**
     * Clears any stale error and reloads data.
     * Called when the composable re-enters composition (e.g. after re-authentication)
     * to ensure a reused ViewModel does not show errors from a previous session.
     */
    fun clearErrorAndReload() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            loadData()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                loadData()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private suspend fun loadData() {
        val breadcrumbs = resolveBreadcrumbs()
        _uiState.update { current ->
            current.copy(
                breadcrumbs = breadcrumbs
            )
        }
        val result = fileRepository.listFiles(path)
        _uiState.update { current ->
            result.fold(
                onSuccess = { entries ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = entries,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    current.copy(
                        breadcrumbs = breadcrumbs,
                        fileEntries = emptyList(),
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    private suspend fun resolveBreadcrumbs(): List<BreadcrumbItem> =
        buildBreadcrumbs(
            path = path,
            homeTitle = getString(Res.string.home_title)
        )
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
