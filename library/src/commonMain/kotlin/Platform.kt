interface Platform {
    val name: PlatformName
}

expect fun getPlatform(): Platform

enum class PlatformName {
    Android, IOS, DESKTOP, WEB
}