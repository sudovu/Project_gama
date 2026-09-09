import SwiftUI

struct LibraryView: View {
    @ObservedObject var audio = AudioManager.shared
    @State private var activeTab: String = "Favorites"

    let tabs = ["Favorites", "Downloaded", "432Hz Resonant"]

    var displayTracks: [GamaTrack] {
        switch activeTab {
        case "Favorites":
            return audio.queue.filter { audio.favorites.contains($0.id) }
        case "Downloaded":
            return audio.queue.filter { audio.downloaded.contains($0.id) }
        case "432Hz Resonant":
            return audio.queue.filter { $0.genre == "Focus & Ambient" }
        default:
            return audio.queue
        }
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // Header
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("LOCAL VAULT")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                        Text("Cyber Library")
                            .font(.system(size: 26, weight: .bold))
                            .foregroundColor(.white)
                    }
                    Spacer()

                    // Quick Shuffle Button
                    Button(action: {
                        if let randomTrack = displayTracks.randomElement() {
                            audio.selectTrack(randomTrack)
                        }
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "shuffle")
                                .font(.caption)
                            Text("Shuffle")
                                .font(.system(size: 13, weight: .bold))
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(red: 18/255, green: 18/255, blue: 26/255))
                        .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                        .cornerRadius(16)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 8)

                // Segmented Tabs
                HStack(spacing: 8) {
                    ForEach(tabs, id: \.self) { tab in
                        Button(action: {
                            activeTab = tab
                        }) {
                            Text(tab)
                                .font(.system(size: 13, weight: .semibold))
                                .padding(.vertical, 8)
                                .frame(maxWidth: .infinity)
                                .background(
                                    activeTab == tab
                                        ? Color(red: 0, green: 1, blue: 0.8)
                                        : Color(red: 18/255, green: 18/255, blue: 26/255)
                                )
                                .foregroundColor(activeTab == tab ? .black : .white)
                                .cornerRadius(14)
                        }
                    }
                }
                .padding(.horizontal)

                // Track Count Bar
                HStack {
                    Text("\(activeTab.uppercased()) (\(displayTracks.count))")
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundColor(.gray)

                    Spacer()

                    Text("OFFLINE STORAGE: READY")
                        .font(.system(size: 9, weight: .bold, design: .monospaced))
                        .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                }
                .padding(.horizontal)
                .padding(.top, 4)

                // Track List or Empty State
                if displayTracks.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "music.note.list")
                            .font(.system(size: 48))
                            .foregroundColor(.gray.opacity(0.5))

                        Text("No Tracks in \(activeTab)")
                            .font(.headline)
                            .foregroundColor(.white)

                        Text("Tap the heart or download icon to pin tracks into your local frequency vault.")
                            .font(.caption)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                        Spacer()
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 4) {
                            ForEach(displayTracks) { track in
                                TrackRowView(track: track)
                            }
                        }
                        .padding(.bottom, 90)
                    }
                }
            }
            .background(Color(red: 10/255, green: 10/255, blue: 15/255).ignoresSafeArea())
            .navigationBarHidden(true)
        }
    }
}
