import SwiftUI
import Ledger

@main
struct iOSApp: App {
    init() {
        LedgerKt.initializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}