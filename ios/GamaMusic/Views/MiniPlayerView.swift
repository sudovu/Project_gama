import SwiftUI

struct MiniPlayerView: View {
    @ObservedObject var audio = AudioManager.shared

    var body: some View {
        VStack(spacing: 0) {
            // Tiny progress indicator line
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .fill(Color.white.opacity(0.1))
                        .frame(height: 2)

                    Rectangle()
                        .fill(
                            LinearGradient(
                                colors: [Color(red: 0, green: 1, blue: 0.8), Color(red: 0.66, green: 0.33, blue: 0.97)],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: geo.size.width * CGFloat(audio.duration > 0 ? (audio.currentTime / audio.duration) : 0), height: 2)
                }
            }
            .frame(height: 2)

            HStack(spacing: 12) {
                // Artwork thumbnail
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
                .frame(width: 46, height: 46)
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(red: 0, green: 1, blue: 0.8).opacity(0.3), lineWidth: 1)
                )

                // Track Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(audio.currentTrack.title)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.white)
                        .lineLimit(1)

                    HStack(spacing: 6) {
                        Text(audio.currentTrack.artist)
                            .font(.system(size: 11))
                            .foregroundColor(.gray)
                            .lineLimit(1)

                        Text("• 432Hz")
                            .font(.system(size: 9, weight: .bold, design: .monospaced))
                            .foregroundColor(Color(red: 0, green: 1, blue: 0.8))
                    }
                }

                Spacer()

                // Play / Pause Button
                Button(action: {
                    audio.togglePlayPause()
                }) {
                    ZStack {
                        Circle()
                            .fill(Color(red: 0, green: 1, blue: 0.8))
                            .frame(width: 38, height: 38)

                        Image(systemName: audio.isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.black)
                            .offset(x: audio.isPlaying ? 0 : 1)
                    }
                }

                // Next Button
                Button(action: {
                    audio.next()
                }) {
                    Image(systemName: "forward.fill")
                        .font(.system(size: 16))
                        .foregroundColor(.white)
                        .frame(width: 32, height: 32)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(
                Color(red: 18/255, green: 18/255, blue: 26/255)
                    .opacity(0.95)
                    .background(.ultraThinMaterial)
            )
        }
        .contentShape(Rectangle())
        .onTapGesture {
            audio.isPlayerExpanded = true
        }
    }
}
