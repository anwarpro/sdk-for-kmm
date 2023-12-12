class IOSPlatform : Platform {
    override val name = PlatformName.IOS
}

actual fun getPlatform(): Platform = IOSPlatform()