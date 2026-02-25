package net.onefivefour.echolist.ui.home

sealed interface InlineCreationState {
    data object Hidden : InlineCreationState
    data class Editing(val name: String = "") : InlineCreationState
    data class Saving(val name: String) : InlineCreationState
    data class Error(val name: String, val message: String) : InlineCreationState
}

data class HomeScreenUiState(
    val title: String,
    val breadcrumbs: List<BreadcrumbItem>,
    val folders: List<FolderUiModel>,
    val files: List<FileUiModel>,
    val inlineCreationState: InlineCreationState = InlineCreationState.Hidden
)

data class BreadcrumbItem(
    val label: String,
    val path: String
)

data class FolderUiModel(
    val id: String,
    val name: String,
    val itemCount: Int
)

data class FileUiModel(
    val id: String,
    val title: String,
    val preview: String,
    val timestamp: String
)
