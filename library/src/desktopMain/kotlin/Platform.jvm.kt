class JVMPlatform : Platform {
    override val name = PlatformName.DESKTOP
}

actual fun getPlatform(): Platform = JVMPlatform()