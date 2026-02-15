package net.onefivefour.notes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.onefivefour.notes.data.models.Note
import net.onefivefour.notes.data.repository.NotesRepository

class HomeViewModel(
    private val path: String,
    private val repository: NotesRepository
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

    private suspend fun loadData() {
        val result = repository.listNotes(path)
        result.fold(
            onSuccess = { notes ->
                _uiState.value = HomeScreenUiState(
                    title = titleFromPath(path),
                    breadcrumbs = buildBreadcrumbs(path),
                    folders = extractFolders(notes, path),
                    files = extractFiles(notes, path)
                )
            },
            onFailure = {
                _uiState.value = HomeScreenUiState(
                    title = titleFromPath(path),
                    breadcrumbs = buildBreadcrumbs(path),
                    folders = emptyList(),
                    files = emptyList()
                )
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
 * Extracts distinct immediate subdirectories from the notes list.
 * A note at path "current/sub/file.md" when current path is "current"
 * means "sub" is a folder.
 */
private fun extractFolders(notes: List<Note>, currentPath: String): List<FolderUiModel> {
    val prefix = if (currentPath == "/") "/" else "$currentPath/"
    val folderMap = mutableMapOf<String, Int>()

    for (note in notes) {
        val relativePath = note.filePath.removePrefix(prefix)
        if (relativePath == note.filePath && currentPath != "/" && currentPath.isNotEmpty()) continue
        if (relativePath.contains('/')) {
            val folderName = relativePath.substringBefore('/')
            folderMap[folderName] = (folderMap[folderName] ?: 0) + 1
        }
    }

    return folderMap.map { (name, count) ->
        val folderPath = if (currentPath == "/") "/$name" else "$currentPath/$name"
        FolderUiModel(id = folderPath, name = name, itemCount = count)
    }
}

/**
 * Extracts notes that are direct children of the current path (no subdirectory).
 */
private fun extractFiles(notes: List<Note>, currentPath: String): List<FileUiModel> {
    val prefix = if (currentPath == "/") "/" else "$currentPath/"

    return notes.filter { note ->
        val relativePath = note.filePath.removePrefix(prefix)
        relativePath != note.filePath || currentPath == "/" || currentPath.isEmpty()
    }.filter { note ->
        val relativePath = note.filePath.removePrefix(prefix)
        !relativePath.contains('/')
    }.map { note ->
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
