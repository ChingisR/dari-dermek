import SwiftUI
import shared

/// Root SwiftUI view that hosts the Compose Multiplatform UI.
/// The Kotlin `MainViewController()` (defined in shared/src/iosMain) calls
/// ComposeUIViewController { App() } which in turn renders GisApp().
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose handles keyboard insets internally
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
