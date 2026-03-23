package net.onefivefour.echolist.data.repository

internal fun normalizePath(path: String): String {
    if (path.isEmpty()) return path
    if (!path.startsWith("//")) return path
    return "/" + path.trimStart('/')
}

internal fun joinPath(parentPath: String, childName: String): String {
    val normalizedChild = childName.trimStart('/')
    return when {
        parentPath.isEmpty() -> normalizePath(normalizedChild)
        parentPath == "/" -> normalizePath("/$normalizedChild")
        else -> normalizePath("${parentPath.trimEnd('/')}/$normalizedChild")
    }
}
