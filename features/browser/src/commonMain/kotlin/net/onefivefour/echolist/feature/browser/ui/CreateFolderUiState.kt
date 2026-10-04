package net.onefivefour.echolist.feature.browser.ui

internal data class CreateFolderUiState(
    val isVisible: Boolean = false,
    val folderName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isConfirmEnabled: Boolean
        get() = folderName.trim().isNotBlank() && !isLoading
}
