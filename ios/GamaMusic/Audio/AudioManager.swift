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

    // Synced Lyrics
    @Published public var syncedLyrics: [LrcLine] = []
    @Published public var isFetchingLyrics: Bool = false
    @Published public var lyricsOffsetSeconds: Double = 0.0

    // Audio / Video Continuity & Smart Library Filters
    @Published public var isAudioOnlyMode: Bool = false
    @Published public var smartFilter: String = "all" // all | hires | stream | video | fav | download

    // UI state
    @Published public var isPlayerExpanded: Bool = false
    @Published public var showEqualizer: Bool = false
    @Published public var showLyricsSheet: Bool = false
    @Published public var showVideoPip: Bool = true

    private var originalQueue: [GamaTrack] = GamaCatalog.tracks

    public init() {
        self.currentTrack = GamaCatalog.tracks[0]
        self.duration = GamaCatalog.tracks[0].duration

        // Initialize background audio session
        GamaAudioSession.shared.setupAudioSession()

        // Configure hardware / Lock Screen controls
        setupRemoteCommands()

        // Fetch initial lyrics
        fetchLyrics(for: GamaCatalog.tracks[0])
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
        fetchLyrics(for: track)
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

    // MARK: - Synchronized LRC Lyrics
    public func fetchLyrics(for track: GamaTrack) {
        guard !track.title.isEmpty else { return }
        isFetchingLyrics = true
        syncedLyrics = []

        guard let encodedTrack = track.title.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let encodedArtist = track.artist.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let url = URL(string: "https://lrclib.net/api/get?track_name=\(encodedTrack)&artist_name=\(encodedArtist)") else {
            isFetchingLyrics = false
            return
        }

        Task {
            do {
                let (data, response) = try await URLSession.shared.data(from: url)
                guard let httpRes = response as? HTTPURLResponse, httpRes.statusCode == 200 else {
                    await MainActor.run { self.isFetchingLyrics = false }
                    return
                }
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let lrc = json["syncedLyrics"] as? String {
                    let parsed = self.parseLrc(lrc)
                    await MainActor.run {
                        self.syncedLyrics = parsed
                        self.isFetchingLyrics = false
                    }
                } else {
                    await MainActor.run { self.isFetchingLyrics = false }
                }
            } catch {
                await MainActor.run { self.isFetchingLyrics = false }
            }
        }
    }

    private func parseLrc(_ text: String) -> [LrcLine] {
        var lines: [LrcLine] = []
        let rawLines = text.components(separatedBy: .newlines)
        let pattern = "\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)"
        guard let regex = try? NSRegularExpression(pattern: pattern) else { return [] }

        for raw in rawLines {
            let nsString = raw as NSString
            let matches = regex.matches(in: raw, range: NSRange(location: 0, length: nsString.length))
            if let match = matches.first, match.numberOfRanges >= 5 {
                let minStr = nsString.substring(with: match.range(at: 1))
                let secStr = nsString.substring(with: match.range(at: 2))
                let msStr = nsString.substring(with: match.range(at: 3))
                let lyricText = nsString.substring(with: match.range(at: 4)).trimmingCharacters(in: .whitespaces)

                if let min = Int(minStr), let sec = Int(secStr) {
                    let msMultiplier = msStr.count == 2 ? 10 : 1
                    let ms = (Int(msStr) ?? 0) * msMultiplier
                    let totalMs = (min * 60 + sec) * 1000 + ms
                    if !lyricText.isEmpty {
                        lines.append(LrcLine(timeMs: totalMs, text: lyricText))
                    }
                }
            }
        }
        return lines
    }
}
