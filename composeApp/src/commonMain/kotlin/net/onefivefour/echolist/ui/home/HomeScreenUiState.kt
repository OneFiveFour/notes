package net.onefivefour.echolist.ui.home

data class HomeScreenUiState(
    val title: String,
    val breadcrumbs: List<BreadcrumbItem>,
    val files: List<FileUiModel>
)

data class BreadcrumbItem(
    val label: String,
    val path: String
)