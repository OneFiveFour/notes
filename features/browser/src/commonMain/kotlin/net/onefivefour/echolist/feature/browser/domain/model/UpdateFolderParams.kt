package net.onefivefour.echolist.feature.browser.domain.model

internal data class UpdateFolderParams(
    val folderPath: String,
    val newName: String
)