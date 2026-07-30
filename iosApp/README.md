# iOS App — Дари-Дармек

## Requirements
| Tool | Minimum version |
|------|----------------|
| macOS | 13 Ventura |
| Xcode | 15 |
| iOS deployment target | 16.0 |
| JDK (for Gradle) | 17 |

## How to build

### 1. Build the shared Kotlin framework
```bash
./gradlew :shared:assembleSharedReleaseXCFramework
```
Or let Xcode do it automatically (it runs `embedAndSignAppleFrameworkForXcode` as a pre-build script).

### 2. Open in Xcode
```bash
open iosApp/iosApp.xcodeproj
```

### 3. Select a simulator or device and press Run (⌘R)

Xcode will:
1. Run the **[CP] Build Kotlin Framework** pre-build script — this calls `./gradlew :shared:embedAndSignAppleFrameworkForXcode` which places the compiled framework under `shared/build/xcode-frameworks/`.
2. Compile the two Swift files (`iOSApp.swift`, `ContentView.swift`).
3. Link `shared.framework` and produce the `.app` bundle.

## Architecture
```
iosApp/
  ContentView.swift      ← SwiftUI wrapper; hosts ComposeUIViewController
  iOSApp.swift           ← @main SwiftUI app entry point
  Info.plist             ← Camera/photo permissions, orientations, bundle ID
  Assets.xcassets/       ← App icon + accent colour

shared/src/iosMain/
  main.ios.kt            ← MainViewController(): UIViewController (Kotlin)
  Platform.ios.kt        ← UIDevice platform name

shared/src/commonMain/
  App.kt                 ← App() → GisApp()  (all platforms share this)
  ui/GisApp.kt           ← Full GIS navigation, login, dashboard, screens
```

## Bundle ID & signing
The bundle identifier is set to `kz.gov.dari.dermek` in the Xcode build settings.  
Set your **Team** under *Signing & Capabilities* before deploying to a real device.

## Known limitations on iOS vs other platforms
- The QR scanner screen shows a placeholder on iOS until a `MLKitBarcodeScanning` bridge is added.
- The bottom navigation adapts automatically to compact-width (iPhone) via `GisAdaptive.kt`.
