package net.onefivefour.echolist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.onefivefour.echolist.data.models.CreateFolderParams
import net.onefivefour.echolist.data.models.Note
import net.onefivefour.echolist.data.repository.FolderRepository
import net.onefivefour.echolist.data.repository.NotesRepository

class HomeViewModel(
    private val path: String,
    private val notesRepository: NotesRepository,
    private val folderRepository: FolderRepository
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
            folderRepository.createFolder(params).fold(
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
        val result = notesRepository.listNotes(path)
        result.fold(
            onSuccess = { listResult ->
                _uiState.update { current ->
                    current.copy(
                        title = titleFromPath(path),
                        breadcrumbs = buildBreadcrumbs(path),
                        folders = extractFolders(listResult.entries, listResult.notes),
                        files = extractFiles(listResult.entries, listResult.notes)
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


private fun titleFromPath(path: String): String {
    if (path == "/" || path.isEmpty()) return "Home"
    return path.trimEnd('/').substringAfterLast('/')
}

private fun buildBreadcrumbs(path: String): List<BreadcrumbItem> {
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
 * Extracts folders from the entries list.
 * Folder entries end with "/". For each folder entry, count how many notes
 * have file paths starting with that folder path.
 */
private fun extractFolders(entries: List<String>, notes: List<Note>): List<FolderUiModel> {
    return entries
        .filter { it.endsWith("/") }
        .map { folderPath ->
            val name = folderPath.trimEnd('/').substringAfterLast('/')
            val itemCount = notes.count { it.filePath.startsWith(folderPath) }
            FolderUiModel(
                id = folderPath,
                name = name,
                itemCount = itemCount
            )
        }
}

/**
 * Extracts files from the entries list.
 * File entries don't end with "/". Matches each entry to its Note for title/content.
 */
private fun extractFiles(entries: List<String>, notes: List<Note>): List<FileUiModel> {
    val notesByPath = notes.associateBy { it.filePath }
    return entries
        .filter { !it.endsWith("/") }
        .mapNotNull { filePath ->
            val note = notesByPath[filePath] ?: return@mapNotNull null
            FileUiModel(
                id = note.filePath,
                title = note.title,
                preview = note.content.take(100),
                timestamp = formatTimestamp(note.updatedAt)
            )
        }
}

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    // Simple relative formatting without kotlinx-datetime
    val seconds = epochMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}