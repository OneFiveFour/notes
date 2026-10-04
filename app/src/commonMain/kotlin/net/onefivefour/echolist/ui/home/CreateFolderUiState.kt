package net.onefivefour.echolist.ui.home

data class CreateFolderUiState(
    val isVisible: Boolean = false,
    val folderName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isConfirmEnabled: Boolean
        get() = folderName.trim().isNotBlank() && !isLoading
}
