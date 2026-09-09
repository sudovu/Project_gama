import SwiftUI

struct PlayerView: View {
    @ObservedObject var audio = AudioManager.shared
    @Environment(\.dismiss) private var dismiss
    @State private var isDraggingSlider = false
    @State private var sliderValue: Double = 0.0

    var body: some View {
        ZStack {
            // Dark cyber background with subtle glow
            Color(red: 10/255, green: 10/255, blue: 15/255)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top Grabber & Controls Bar
                HStack {
                    Button(action: {
                        audio.isPlayerExpanded = false
                    }) {
                        Image(systemName: "chevron.down")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.gray)
                    }

                    Spacer()

                    VStack(spacing: 2) {
                        Text("PLAYING FROM FREQUENCY MATRIX")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))

                        Text(audio.currentTrack.album)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.white)
                            .lineLimit(1)
                    }

                    Spacer()

                    Button(action: {
                        audio.showEqualizer = true
                    }) {
                        Image(systemName: "slider.vertical.3")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)

                Spacer()

                // Glowing Album Artwork or Embedded Video
                ZStack {
                    AsyncImage(url: URL(string: audio.currentTrack.artworkUrl)) { phase in
                        if let image = phase.image {
                            image
                                .resizable()
                                .scaledToFill()
                        } else {
                            Rectangle()
                                .fill(Color(red: 28/255, green: 28/255, blue: 40/255))
                        }
                    }
                    .frame(width: 310, height: 310)
                    .cornerRadius(24)
                    .overlay(
                        RoundedRectangle(cornerRadius: 24)
                            .stroke(
                                LinearGradient(
                                    colors: [Color(red: 0, green: 1, blue: 0.8).opacity(0.8), Color(red: 0.66, green: 0.33, blue: 0.97).opacity(0.5)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 2
                            )
                    )
                    .shadow(color: Color(red: 0, green: 1, blue: 0.8).opacity(0.25), radius: 24, x: 0, y: 8)

                    // Embedded mini player view for YouTube pipeline
                    YouTubePlayerView(
                        videoId: audio.currentTrack.youtubeVideoId,
                        isPlaying: audio.isPlaying,
                        onReady: {
                            print("[PlayerView] YouTube Player Ready")
                        },
                        onStateChange: { state in
                            audio.handlePlayerStateChange(state)
                        },
                        onTimeUpdate: { time, dur in
                            if !isDraggingSlider {
                                audio.updateTime(time: time, dur: dur)
                            }
                        }
                    )
                    .frame(width: 310, height: 310)
                    .cornerRadius(24)
                    .opacity(audio.showVideoPip ? 1.0 : 0.001)
                }

                Spacer()

                // Track Metadata & Favorite
                HStack(alignment: .center) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(audio.currentTrack.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.white)
                            .lineLimit(1)

                        HStack(spacing: 8) {
                            Text(audio.currentTrack.artist)
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.gray)
                                .lineLimit(1)

                            Text(audio.currentTrack.genre)
                                .font(.system(size: 10, weight: .bold, design: .monospaced))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color(red: 28/255, green: 28/255, blue: 40/255))
                                .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                                .cornerRadius(8)
                        }
                    }

                    Spacer()

                    Button(action: {
                        audio.toggleFavorite(audio.currentTrack.id)
                    }) {
                        Image(systemName: audio.favorites.contains(audio.currentTrack.id) ? "heart.fill" : "heart")
                            .font(.title2)
                            .foregroundColor(audio.favorites.contains(audio.currentTrack.id) ? Color(red: 1.0, green: 0.2, blue: 0.5) : .gray)
                    }
                }
                .padding(.horizontal, 28)

                // Seek Slider & Timestamps
                VStack(spacing: 6) {
                    Slider(
                        value: Binding(
                            get: { isDraggingSlider ? sliderValue : audio.currentTime },
                            set: { val in
                                sliderValue = val
                                isDraggingSlider = true
                            }
                        ),
                        in: 0...(audio.duration > 0 ? audio.duration : 100),
                        onEditingChanged: { editing in
                            isDraggingSlider = editing
                            if !editing {
                                audio.seek(to: sliderValue)
                            }
                        }
                    )
                    .accentColor(Color(red: 0, green: 1, blue: 0.8))

                    HStack {
                        Text(formatSeconds(isDraggingSlider ? sliderValue : audio.currentTime))
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundColor(.gray)

                        Spacer()

                        Text(formatSeconds(audio.duration))
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundColor(.gray)
                    }
                }
                .padding(.horizontal, 28)
                .padding(.top, 14)

                // Playback Control Buttons
                HStack(spacing: 36) {
                    // Shuffle
                    Button(action: {
                        audio.toggleShuffle()
                    }) {
                        Image(systemName: "shuffle")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(audio.isShuffle ? Color(red: 0, green: 1, blue: 0.8) : .gray)
                    }

                    // Previous
                    Button(action: {
                        audio.previous()
                    }) {
                        Image(systemName: "backward.fill")
                            .font(.system(size: 26))
                            .foregroundColor(.white)
                    }

                    // Play / Pause (Large glowing button)
                    Button(action: {
                        audio.togglePlayPause()
                    }) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color(red: 0, green: 1, blue: 0.8), Color(red: 0.66, green: 0.33, blue: 0.97)],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 72, height: 72)
                                .shadow(color: Color(red: 0, green: 1, blue: 0.8).opacity(0.4), radius: 16)

                            Image(systemName: audio.isPlaying ? "pause.fill" : "play.fill")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundColor(.black)
                                .offset(x: audio.isPlaying ? 0 : 2)
                        }
                    }

                    // Next
                    Button(action: {
                        audio.next()
                    }) {
                        Image(systemName: "forward.fill")
                            .font(.system(size: 26))
                            .foregroundColor(.white)
                    }

                    // Repeat Mode
                    Button(action: {
                        switch audio.repeatMode {
                        case .off: audio.repeatMode = .all
                        case .all: audio.repeatMode = .one
                        case .one: audio.repeatMode = .off
                        }
                    }) {
                        Image(systemName: audio.repeatMode == .one ? "repeat.1" : "repeat")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(audio.repeatMode != .off ? Color(red: 1.0, green: 0.2, blue: 0.5) : .gray)
                    }
                }
                .padding(.top, 16)

                // Bottom Utilities (Volume + PiP Toggle)
                HStack(spacing: 16) {
                    Image(systemName: "speaker.fill")
                        .foregroundColor(.gray)
                        .font(.caption)

                    Slider(value: $audio.volume, in: 0...1)
                        .accentColor(Color(red: 0, green: 1, blue: 0.8))

                    Image(systemName: "speaker.wave.3.fill")
                        .foregroundColor(.gray)
                        .font(.caption)

                    Button(action: {
                        audio.showVideoPip.toggle()
                    }) {
                        Image(systemName: audio.showVideoPip ? "pip.exit" : "pip.enter")
                            .foregroundColor(audio.showVideoPip ? Color(red: 0, green: 1, blue: 0.8) : .gray)
                            .font(.system(size: 14))
                    }
                }
                .padding(.horizontal, 32)
                .padding(.top, 24)
                .padding(.bottom, 24)
            }
        }
        .sheet(isPresented: $audio.showEqualizer) {
            EqualizerView()
        }
    }

    private func formatSeconds(_ seconds: Double) -> String {
        let total = Int(seconds)
        let m = total / 60
        let s = total % 60
        return String(format: "%d:%02d", m, s)
    }
}
