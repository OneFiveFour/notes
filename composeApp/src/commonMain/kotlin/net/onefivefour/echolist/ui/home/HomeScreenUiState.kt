package net.onefivefour.echolist.ui.home

data class HomeScreenUiState(
    val title: String,
    val breadcrumbs: List<BreadcrumbItem>,
    val folders: List<FolderUiModel>,
    val files: List<FileUiModel>
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
