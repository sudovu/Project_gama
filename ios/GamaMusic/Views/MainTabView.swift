import SwiftUI

struct MainTabView: View {
    @ObservedObject var audio = AudioManager.shared
    @State private var selectedTab: Int = 0

    init() {
        // Configure dark translucent tab bar appearance
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor(red: 10/255, green: 10/255, blue: 15/255, alpha: 0.96)
        UITabBar.appearance().standardAppearance = appearance
        if #available(iOS 15.0, *) {
            UITabBar.appearance().scrollEdgeAppearance = appearance
        }
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $selectedTab) {
                DiscoverView()
                    .tabItem {
                        Label("Discover", systemImage: "sparkles")
                    }
                    .tag(0)

                SearchView()
                    .tabItem {
                        Label("Search", systemImage: "magnifyingglass")
                    }
                    .tag(1)

                LibraryView()
                    .tabItem {
                        Label("Library", systemImage: "music.note.list")
                    }
                    .tag(2)

                ProfileView()
                    .tabItem {
                        Label("Architect", systemImage: "person.crop.circle")
                    }
                    .tag(3)
            }
            .accentColor(Color(red: 0, green: 1, blue: 0.8))

            // Floating Mini-Player Bar (pinned directly above TabBar)
            MiniPlayerView()
                .padding(.bottom, 49) // Standard iOS TabBar height
        }
        .fullScreenCover(isPresented: $audio.isPlayerExpanded) {
            PlayerView()
        }
    }
}
