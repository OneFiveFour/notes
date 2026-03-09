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
import net.onefivefour.echolist.domain.repository.FileRepository
import org.jetbrains.compose.resources.getString

class HomeViewModel(
    private val path: String,
    private val fileRepository: FileRepository
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
    }

    private suspend fun loadData() {
        val homeTitle = getString(Res.string.home_title)
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
    val breadcrumbs = mutableListOf(BreadcrumbItem(label = homeTitle, path = "/"))
    if (path == "/" || path.isEmpty()) return breadcrumbs

    val segments = path.trimStart('/').trimEnd('/').split('/')
    var accumulated = ""
    for (segment in segments) {
        accumulated = "$accumulated/$segment"
        breadcrumbs.add(BreadcrumbItem(label = segment, path = accumulated))
    }
    return breadcrumbs
}