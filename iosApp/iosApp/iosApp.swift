import SwiftUI
import Ledger

@main
struct iOSApp: App {
    init() {
        initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}