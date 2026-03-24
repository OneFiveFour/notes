package net.onefivefour.echolist.ui.home

import net.onefivefour.echolist.data.models.FileEntry

data class HomeScreenUiState(
    val breadcrumbs: List<BreadcrumbItem>,
    val fileEntries: List<FileEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

data class BreadcrumbItem(
    val label: String,
    val path: String
)