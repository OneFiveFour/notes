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

class HomeViewModel(
    private val path: String,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeScreenUiState(
            title = "",
            breadcrumbs = emptyList(),
            folders = emptyList(),
            files = emptyList()
        )
    )
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    fun onAddFolderClicked() {
        _uiState.update { it.copy(inlineCreationState = InlineCreationState.Editing()) }
    }

    fun onInlineNameChanged(name: String) {
        val current = _uiState.value.inlineCreationState
        if (current is InlineCreationState.Editing) {
            _uiState.update { it.copy(inlineCreationState = InlineCreationState.Editing(name)) }
        }
    }

    fun onInlineConfirm() {
        val current = _uiState.value.inlineCreationState
        if (current !is InlineCreationState.Editing) return

        val trimmedName = current.name.trim()
        if (trimmedName.isBlank()) return

        _uiState.update { it.copy(inlineCreationState = InlineCreationState.Saving(trimmedName)) }

        viewModelScope.launch {
            val params = CreateFolderParams(
                parentPath = path,
                name = trimmedName
            )
            fileRepository.createFolder(params).fold(
                onSuccess = {
                    loadData()
                    _uiState.update { it.copy(inlineCreationState = InlineCreationState.Hidden) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            inlineCreationState = InlineCreationState.Error(
                                name = trimmedName,
                                message = error.message ?: "Folder creation failed"
                            )
                        )
                    }
                }
            )
        }
    }

    fun onInlineCancel() {
        _uiState.update { it.copy(inlineCreationState = InlineCreationState.Hidden) }
    }

    private suspend fun loadData() {
        val result = fileRepository.listFiles(path)
        result.fold(
            onSuccess = { entries ->
                _uiState.update { current ->
                    current.copy(
                        title = titleFromPath(path),
                        breadcrumbs = buildBreadcrumbs(path),
                        folders = extractFolders(entries),
                        files = extractFiles(entries)
                    )
                }
            },
            onFailure = {
                _uiState.update { current ->
                    current.copy(
                        title = titleFromPath(path),
                        breadcrumbs = buildBreadcrumbs(path),
                        folders = emptyList(),
                        files = emptyList()
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

/**
 * Extracts folder entries (paths ending with "/") and builds FolderUiModels.
 * itemCount is 0 since string entries don't carry sub-item counts.
 */
internal fun extractFolders(entries: List<String>): List<FolderUiModel> {
    return entries
        .filter { it.endsWith("/") }
        .map { folderPath ->
            val name = folderPath.trimEnd('/').substringAfterLast('/')
            FolderUiModel(
                id = folderPath,
                name = name,
                itemCount = 0
            )
        }
}

/**
 * Extracts file entries (paths not ending with "/") and builds FileUiModels.
 * Detects file type from prefix: "note_" → NOTE, "tasks_" → TASK_LIST.
 * Derives display title by stripping the prefix.
 */
internal fun extractFiles(entries: List<String>): List<FileUiModel> {
    return entries
        .filter { !it.endsWith("/") }
        .map { filePath ->
            val fileName = filePath.substringAfterLast('/')
            val (fileType, title) = when {
                fileName.startsWith("note_") -> FileType.NOTE to fileName.removePrefix("note_")
                fileName.startsWith("tasks_") -> FileType.TASK_LIST to fileName.removePrefix("tasks_")
                else -> FileType.NOTE to fileName
            }
            FileUiModel(
                id = filePath,
                title = title,
                fileType = fileType,
                preview = "",
                timestamp = ""
            )
        }
}
