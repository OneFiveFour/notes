package net.onefivefour.echolist.data.models

data class CreateFolderParams(
    val domain: String,
    val parentPath: String,
    val name: String
)
