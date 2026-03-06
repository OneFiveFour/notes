package net.onefivefour.echolist.ui.home

data class HomeScreenUiState(
    val breadcrumbs: List<BreadcrumbItem>
)

data class BreadcrumbItem(
    val label: String,
    val path: String
)