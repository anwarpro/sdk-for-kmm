class AndroidPlatform : Platform {
    override val name: PlatformName = PlatformName.Android
}

actual fun getPlatform(): Platform = AndroidPlatform()