package dev.kekulta.chia

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform