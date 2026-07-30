package com.dari.dermek

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    // App() delegates to com.dari.dermek.ui.GisApp() — same entry as desktop and web.
    App()
}
