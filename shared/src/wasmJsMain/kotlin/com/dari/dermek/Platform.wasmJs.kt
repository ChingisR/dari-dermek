package com.dari.dermek

class WasmPlatform : Platform {
    override val name: String = "Web browser (Kotlin/Wasm)"
}

actual fun getPlatform(): Platform = WasmPlatform()
