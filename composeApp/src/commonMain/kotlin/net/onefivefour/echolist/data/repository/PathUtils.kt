package net.onefivefour.echolist.data.repository

internal fun normalizePath(path: String): String {
    return path
        .replace('\\', '/')
        .trimStart('/')
        .trimEnd('/')
        .replace(Regex("/+"), "/")
        .let { if (it == ".") "" else it }
}

internal fun joinPath(parentPath: String, childName: String): String {
    val parent = normalizePath(parentPath)
    val child = normalizePath(childName)
    return when {
        parent.isEmpty() -> child
        child.isEmpty() -> parent
        else -> "$parent/$child"
    }
}
