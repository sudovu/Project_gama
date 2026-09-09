import Foundation
import MediaPlayer
import UIKit

/// Manages Lock Screen, Control Center, Dynamic Island and Apple Watch Now Playing controls
public final class NowPlayingManager {
    public static let shared = NowPlayingManager()

    private init() {}

    /// Update lock screen / control center metadata
    public func updateNowPlayingInfo(track: GamaTrack?, isPlaying: Bool, currentTime: Double) {
        guard let track = track else {
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
            return
        }

        var info = [String: Any]()
        info[MPMediaItemPropertyTitle] = track.title
        info[MPMediaItemPropertyArtist] = track.artist
        info[MPMediaItemPropertyAlbumTitle] = "\(track.album) (432Hz)"
        info[MPMediaItemPropertyPlaybackDuration] = track.duration
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0

        // Asynchronously load artwork
        if let url = URL(string: track.artworkUrl) {
            URLSession.shared.dataTask(with: url) { data, _, _ in
                if let data = data, let image = UIImage(data: data) {
                    DispatchQueue.main.async {
                        var updatedInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? info
                        let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
                        updatedInfo[MPMediaItemPropertyArtwork] = artwork
                        MPNowPlayingInfoCenter.default().nowPlayingInfo = updatedInfo
                    }
                }
            }.resume()
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    /// Register hardware and control center remote commands
    public func setupRemoteCommands(
        onPlay: @escaping () -> Void,
        onPause: @escaping () -> Void,
        onToggle: @escaping () -> Void,
        onNext: @escaping () -> Void,
        onPrevious: @escaping () -> Void,
        onSeek: @escaping (Double) -> Void
    ) {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { _ in
            onPlay()
            return .success
        }

        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { _ in
            onPause()
            return .success
        }

        commandCenter.togglePlayPauseCommand.isEnabled = true
        commandCenter.togglePlayPauseCommand.addTarget { _ in
            onToggle()
            return .success
        }

        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.nextTrackCommand.addTarget { _ in
            onNext()
            return .success
        }

        commandCenter.previousTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.addTarget { _ in
            onPrevious()
            return .success
        }

        commandCenter.changePlaybackPositionCommand.isEnabled = true
        commandCenter.changePlaybackPositionCommand.addTarget { event in
            guard let positionEvent = event as? MPChangePlaybackPositionCommandEvent else {
                return .commandFailed
            }
            onSeek(positionEvent.positionTime)
            return .success
        }
    }
}
