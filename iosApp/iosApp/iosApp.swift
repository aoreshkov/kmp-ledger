import SwiftUI
import Ledger

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}