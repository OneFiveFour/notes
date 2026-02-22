package net.onefivefour.echolist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform