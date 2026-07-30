package com.dari.dermek

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
