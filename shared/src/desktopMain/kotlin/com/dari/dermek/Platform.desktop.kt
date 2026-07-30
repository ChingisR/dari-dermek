package com.dari.dermek

class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM (Java " + System.getProperty("java.version") + ")"
}

actual fun getPlatform(): Platform = DesktopPlatform()
