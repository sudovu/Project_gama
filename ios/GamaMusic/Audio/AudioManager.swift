import Foundation
import SwiftUI
import Combine

public enum RepeatMode: String, CaseIterable {
    case off = "off"
    case all = "all"
    case one = "one"
}

/// Central Audio and Playback State Coordinator for GAMA iOS
@MainActor
public final class AudioManager: ObservableObject {
    public static let shared = AudioManager()

    // MARK: - Published Properties
    @Published public var currentTrack: GamaTrack
    @Published public var queue: [GamaTrack] = GamaCatalog.tracks
    @Published public var isPlaying: Bool = false
    @Published public var currentTime: Double = 0.0
    @Published public var duration: Double = 216.0
    @Published public var volume: Double = 0.8
    @Published public var isMuted: Bool = false
    @Published public var isShuffle: Bool = false
    @Published public var repeatMode: RepeatMode = .off

    // Equalizer
    @Published public var eqGains: [Double] = [0.0, 0.0, 0.0, 0.0, 0.0]
    @Published public var currentPreset: String = "Flat"
    @Published public var is432HzHarmonic: Bool = true

    // Library
    @Published public var favorites: Set<String> = ["trk_em_01", "trk_sk_01"]
    @Published public var downloaded: Set<String> = ["trk_em_01"]

    // UI state
    @Published public var isPlayerExpanded: Bool = false
    @Published public var showEqualizer: Bool = false
    @Published public var showVideoPip: Bool = true

    private var originalQueue: [GamaTrack] = GamaCatalog.tracks

    public init() {
        self.currentTrack = GamaCatalog.tracks[0]
        self.duration = GamaCatalog.tracks[0].duration

        // Initialize background audio session
        GamaAudioSession.shared.setupAudioSession()

        // Configure hardware / Lock Screen controls
        setupRemoteCommands()
    }

    private func setupRemoteCommands() {
        NowPlayingManager.shared.setupRemoteCommands(
            onPlay: { [weak self] in
                Task { @MainActor in
                    self?.play()
                }
            },
            onPause: { [weak self] in
                Task { @MainActor in
                    self?.pause()
                }
            },
            onToggle: { [weak self] in
                Task { @MainActor in
                    self?.togglePlayPause()
                }
            },
            onNext: { [weak self] in
                Task { @MainActor in
                    self?.next()
                }
            },
            onPrevious: { [weak self] in
                Task { @MainActor in
                    self?.previous()
                }
            },
            onSeek: { [weak self] targetSec in
                Task { @MainActor in
                    self?.seek(to: targetSec)
                }
            }
        )
    }

    // MARK: - Playback Control
    public func selectTrack(_ track: GamaTrack) {
        currentTrack = track
        duration = track.duration
        currentTime = 0.0
        isPlaying = true
        updateNowPlaying()
    }

    public func togglePlayPause() {
        isPlaying.toggle()
        updateNowPlaying()
    }

    public func play() {
        isPlaying = true
        updateNowPlaying()
    }

    public func pause() {
        isPlaying = false
        updateNowPlaying()
    }

    public func next() {
        guard !queue.isEmpty else { return }
        if let idx = queue.firstIndex(where: { $0.id == currentTrack.id }) {
            let nextIdx = (idx + 1) % queue.count
            selectTrack(queue[nextIdx])
        } else {
            selectTrack(queue[0])
        }
    }

    public func previous() {
        guard !queue.isEmpty else { return }
        if currentTime > 3.0 {
            seek(to: 0.0)
            return
        }
        if let idx = queue.firstIndex(where: { $0.id == currentTrack.id }) {
            let prevIdx = (idx - 1 + queue.count) % queue.count
            selectTrack(queue[prevIdx])
        } else {
            selectTrack(queue[0])
        }
    }

    public func seek(to seconds: Double) {
        currentTime = max(0, min(seconds, duration))
        updateNowPlaying()
    }

    public func toggleShuffle() {
        isShuffle.toggle()
        if isShuffle {
            queue = queue.shuffled()
        } else {
            queue = originalQueue
        }
    }

    public func toggleFavorite(_ trackId: String) {
        if favorites.contains(trackId) {
            favorites.remove(trackId)
        } else {
            favorites.insert(trackId)
        }
    }

    public func toggleDownload(_ trackId: String) {
        if downloaded.contains(trackId) {
            downloaded.remove(trackId)
        } else {
            downloaded.insert(trackId)
        }
    }

    public func applyPreset(_ preset: EqualizerPreset) {
        currentPreset = preset.name
        eqGains = preset.gains
    }

    public func updateTime(time: Double, dur: Double) {
        self.currentTime = time
        if dur > 0 && abs(self.duration - dur) > 1.0 {
            self.duration = dur
        }
        updateNowPlaying()
    }

    public func handlePlayerStateChange(_ state: Int) {
        // YT.PlayerState: 1 = PLAYING, 2 = PAUSED, 0 = ENDED
        if state == 1 {
            self.isPlaying = true
        } else if state == 2 {
            self.isPlaying = false
        } else if state == 0 {
            if repeatMode == .one {
                seek(to: 0.0)
                play()
            } else {
                next()
            }
        }
        updateNowPlaying()
    }

    private func updateNowPlaying() {
        NowPlayingManager.shared.updateNowPlayingInfo(
            track: currentTrack,
            isPlaying: isPlaying,
            currentTime: currentTime
        )
    }
}
