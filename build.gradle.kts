plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

allprojects {
    if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
        layout.buildDirectory.set(file("C:/temp/dari-dermek-build/${project.name}"))
    }
}
