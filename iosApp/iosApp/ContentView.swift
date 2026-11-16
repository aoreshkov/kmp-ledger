import UIKit
import SwiftUI
import Ledger

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            // Deep links on the app's own scheme (CFBundleURLTypes in Info.plist),
            // e.g. ledger://postings/42. The paths are matched in shared code.
            .onOpenURL { url in
                openDeepLink(url: url.absoluteString)
            }
    }
}