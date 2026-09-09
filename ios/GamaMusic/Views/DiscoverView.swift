import SwiftUI

struct DiscoverView: View {
    @ObservedObject var audio = AudioManager.shared
    @State private var selectedGenre: String = "All"

    var filteredTracks: [GamaTrack] {
        if selectedGenre == "All" {
            return audio.queue
        }
        return audio.queue.filter { $0.genre == selectedGenre }
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Header Brand
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("CYBERNETIC AUDIO")
                                .font(.system(size: 10, weight: .bold, design: .monospaced))
                                .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                            Text("GAMA Discover")
                                .font(.system(size: 26, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Spacer()

                        Button(action: {
                            audio.showEqualizer = true
                        }) {
                            ZStack {
                                Circle()
                                    .fill(Color(red: 18/255, green: 18/255, blue: 26/255))
                                    .frame(width: 40, height: 40)

                                Image(systemName: "slider.vertical.3")
                                    .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 8)

                    // Hero Featured Banner
                    if let featured = audio.queue.first {
                        ZStack(alignment: .bottomLeading) {
                            AsyncImage(url: URL(string: featured.artworkUrl)) { phase in
                                if let image = phase.image {
                                    image
                                        .resizable()
                                        .scaledToFill()
                                } else {
                                    Rectangle().fill(Color(red: 28/255, green: 28/255, blue: 40/255))
                                }
                            }
                            .frame(height: 180)
                            .clipped()

                            // Gradient Overlay
                            LinearGradient(
                                colors: [.clear, Color.black.opacity(0.85)],
                                startPoint: .top,
                                endPoint: .bottom
                            )

                            VStack(alignment: .leading, spacing: 6) {
                                Text("FEATURED HARMONIC")
                                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(Color(red: 0, green: 1, blue: 0.8))
                                    .foregroundColor(.black)
                                    .cornerRadius(6)

                                Text(featured.title)
                                    .font(.title2)
                                    .fontWeight(.bold)
                                    .foregroundColor(.white)

                                Text("\(featured.artist) • \(featured.album)")
                                    .font(.subheadline)
                                    .foregroundColor(Color.white.opacity(0.8))

                                Button(action: {
                                    audio.selectTrack(featured)
                                }) {
                                    HStack(spacing: 6) {
                                        Image(systemName: "play.fill")
                                            .font(.caption)
                                        Text("Play Frequency")
                                            .font(.system(size: 13, weight: .bold))
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(
                                        LinearGradient(
                                            colors: [Color(red: 0, green: 1, blue: 0.8), Color(red: 0.66, green: 0.33, blue: 0.97)],
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                                    .foregroundColor(.black)
                                    .cornerRadius(20)
                                }
                                .padding(.top, 4)
                            }
                            .padding(16)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 180)
                        .cornerRadius(20)
                        .padding(.horizontal)
                    }

                    // Genre Selector Chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(GamaGenre.all, id: \.self) { genre in
                                Button(action: {
                                    selectedGenre = genre
                                }) {
                                    Text(genre)
                                        .font(.system(size: 13, weight: .semibold))
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 8)
                                        .background(
                                            selectedGenre == genre
                                                ? Color(red: 0, green: 1, blue: 0.8)
                                                : Color(red: 18/255, green: 18/255, blue: 26/255)
                                        )
                                        .foregroundColor(selectedGenre == genre ? .black : .white)
                                        .cornerRadius(18)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // Track Grid / List
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text("FREQUENCY NODES (\(filteredTracks.count))")
                                .font(.system(size: 11, weight: .bold, design: .monospaced))
                                .foregroundColor(.gray)
                            Spacer()
                        }
                        .padding(.horizontal)

                        ForEach(filteredTracks) { track in
                            TrackRowView(track: track)
                        }
                    }

                    Spacer(minLength: 90)
                }
            }
            .background(Color(red: 10/255, green: 10/255, blue: 15/255).ignoresSafeArea())
            .navigationBarHidden(true)
        }
    }
}

// Reusable Track Row
struct TrackRowView: View {
    let track: GamaTrack
    @ObservedObject var audio = AudioManager.shared

    var isCurrent: Bool {
        audio.currentTrack.id == track.id
    }

    var body: some View {
        HStack(spacing: 12) {
            // Artwork
            AsyncImage(url: URL(string: track.artworkUrl)) { phase in
                if let image = phase.image {
                    image.resizable().scaledToFill()
                } else {
                    Rectangle().fill(Color(red: 28/255, green: 28/255, blue: 40/255))
                }
            }
            .frame(width: 50, height: 50)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isCurrent ? Color(red: 0, green: 1, blue: 0.8) : Color.clear, lineWidth: 1.5)
            )

            // Info
            VStack(alignment: .leading, spacing: 3) {
                Text(track.title)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(isCurrent ? Color(red: 0, green: 1, blue: 0.8) : .white)
                    .lineLimit(1)

                HStack(spacing: 6) {
                    Text(track.artist)
                        .font(.system(size: 12))
                        .foregroundColor(.gray)
                        .lineLimit(1)

                    Text("• \(track.genre)")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundColor(.gray.opacity(0.8))
                }
            }

            Spacer()

            // Play / Waveform indicator
            if isCurrent && audio.isPlaying {
                Image(systemName: "waveform")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
            }

            // Favorite Button
            Button(action: {
                audio.toggleFavorite(track.id)
            }) {
                Image(systemName: audio.favorites.contains(track.id) ? "heart.fill" : "heart")
                    .foregroundColor(audio.favorites.contains(track.id) ? Color(red: 1.0, green: 0.2, blue: 0.5) : .gray)
            }
            .padding(.trailing, 4)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(
            isCurrent
                ? Color(red: 18/255, green: 18/255, blue: 26/255)
                : Color.clear
        )
        .cornerRadius(14)
        .padding(.horizontal, 10)
        .contentShape(Rectangle())
        .onTapGesture {
            audio.selectTrack(track)
        }
    }
}
