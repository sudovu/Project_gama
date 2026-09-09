import SwiftUI

@main
struct GamaMusicApp: App {
    @StateObject private var audio = AudioManager.shared

    var body: some Scene {
        WindowGroup {
            MainTabView()
                .preferredColorScheme(.dark)
                .environmentObject(audio)
        }
    }
}
