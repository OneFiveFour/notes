package net.onefivefour.echolist.feature.browser.ui

import net.onefivefour.echolist.feature.browser.domain.model.FileEntry

internal data class BrowserUiState(
    val breadcrumbs: List<BreadcrumbItem>,
    val fileEntries: List<FileEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDeletingFolder: Boolean = false,
    val canDeleteCurrentFolder: Boolean = false,
    val canRenameCurrentFolder: Boolean = false,
    val error: String? = null
)

internal data class BreadcrumbItem(
    val label: String,
    val parentDir: String
)