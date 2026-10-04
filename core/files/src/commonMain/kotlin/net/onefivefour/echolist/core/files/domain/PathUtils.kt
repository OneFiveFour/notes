package net.onefivefour.echolist.core.files.domain

fun normalizePath(path: String): String {
    return path
        .replace('\\', '/')
        .trimStart('/')
        .trimEnd('/')
        .replace(Regex("/+"), "/")
        .let { if (it == ".") "" else it }
}

fun joinPath(parentDir: String, childName: String): String {
    val parent = normalizePath(parentDir)
    val child = normalizePath(childName)
    return when {
        parent.isEmpty() -> child
        child.isEmpty() -> parent
        else -> "$parent/$child"
    }
}