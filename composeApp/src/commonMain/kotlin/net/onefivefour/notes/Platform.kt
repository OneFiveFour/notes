package net.onefivefour.notes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform