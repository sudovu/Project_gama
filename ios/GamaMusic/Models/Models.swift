import Foundation

// MARK: - Track Model
public struct GamaTrack: Identifiable, Codable, Hashable {
    public let id: String
    public let title: String
    public let artist: String
    public let album: String
    public let duration: Double // in seconds
    public let artworkUrl: String
    public let youtubeVideoId: String
    public let genre: String
    public let frequencyHz: Int

    public init(
        id: String,
        title: String,
        artist: String,
        album: String,
        duration: Double,
        artworkUrl: String,
        youtubeVideoId: String,
        genre: String,
        frequencyHz: Int = 432
    ) {
        self.id = id
        self.title = title
        self.artist = artist
        self.album = album
        self.duration = duration
        self.artworkUrl = artworkUrl
        self.youtubeVideoId = youtubeVideoId
        self.genre = genre
        self.frequencyHz = frequencyHz
    }
}

// MARK: - Genres
public enum GamaGenre {
    public static let all: [String] = [
        "All",
        "Rock & Metal",
        "Pop / Synthwave",
        "Cyberpunk / Synth",
        "Hip-Hop",
        "Focus & Ambient"
    ]
}

// MARK: - Equalizer Preset
public struct EqualizerPreset: Identifiable, Hashable {
    public var id: String { name }
    public let name: String
    public let gains: [Double] // 5 bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz

    public static let presets: [EqualizerPreset] = [
        EqualizerPreset(name: "Flat", gains: [0.0, 0.0, 0.0, 0.0, 0.0]),
        EqualizerPreset(name: "Bass Boost", gains: [6.0, 4.5, 1.0, 0.0, -1.0]),
        EqualizerPreset(name: "Cyber Rock", gains: [4.0, 2.0, -1.5, 3.5, 5.0]),
        EqualizerPreset(name: "Vocal Clarity", gains: [-2.0, 1.0, 4.5, 3.0, 1.0]),
        EqualizerPreset(name: "Electronic", gains: [5.5, 3.0, 0.0, 2.5, 4.5])
    ]
}

// MARK: - Initial Canonical 20 Tracks
public enum GamaCatalog {
    public static let tracks: [GamaTrack] = [
        // Rock & Metal
        GamaTrack(
            id: "trk_lp_01",
            title: "In the End",
            artist: "Linkin Park",
            album: "Hybrid Theory",
            duration: 216,
            artworkUrl: "https://i.ytimg.com/vi/eVTXPUF4Oz4/hqdefault.jpg",
            youtubeVideoId: "eVTXPUF4Oz4",
            genre: "Rock & Metal"
        ),
        GamaTrack(
            id: "trk_lp_02",
            title: "Numb",
            artist: "Linkin Park",
            album: "Meteora",
            duration: 187,
            artworkUrl: "https://i.ytimg.com/vi/kXYiU_JCYtU/hqdefault.jpg",
            youtubeVideoId: "kXYiU_JCYtU",
            genre: "Rock & Metal"
        ),
        GamaTrack(
            id: "trk_met_01",
            title: "Enter Sandman",
            artist: "Metallica",
            album: "Metallica (Black Album)",
            duration: 331,
            artworkUrl: "https://i.ytimg.com/vi/CD-E-LDc384/hqdefault.jpg",
            youtubeVideoId: "CD-E-LDc384",
            genre: "Rock & Metal"
        ),
        GamaTrack(
            id: "trk_nir_01",
            title: "Smells Like Teen Spirit",
            artist: "Nirvana",
            album: "Nevermind",
            duration: 301,
            artworkUrl: "https://i.ytimg.com/vi/hTWKbfoikeg/hqdefault.jpg",
            youtubeVideoId: "hTWKbfoikeg",
            genre: "Rock & Metal"
        ),

        // Pop / Synthwave
        GamaTrack(
            id: "trk_wknd_01",
            title: "Blinding Lights",
            artist: "The Weeknd",
            album: "After Hours",
            duration: 200,
            artworkUrl: "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            youtubeVideoId: "4NRXx6U8ABQ",
            genre: "Pop / Synthwave"
        ),
        GamaTrack(
            id: "trk_dua_01",
            title: "Levitating",
            artist: "Dua Lipa",
            album: "Future Nostalgia",
            duration: 203,
            artworkUrl: "https://i.ytimg.com/vi/TUVcZfQe-Kw/hqdefault.jpg",
            youtubeVideoId: "TUVcZfQe-Kw",
            genre: "Pop / Synthwave"
        ),
        GamaTrack(
            id: "trk_wknd_02",
            title: "Starboy",
            artist: "The Weeknd ft. Daft Punk",
            album: "Starboy",
            duration: 230,
            artworkUrl: "https://i.ytimg.com/vi/34Na4j8AVgA/hqdefault.jpg",
            youtubeVideoId: "34Na4j8AVgA",
            genre: "Pop / Synthwave"
        ),
        GamaTrack(
            id: "trk_dp_01",
            title: "Get Lucky",
            artist: "Daft Punk ft. Pharrell Williams",
            album: "Random Access Memories",
            duration: 248,
            artworkUrl: "https://i.ytimg.com/vi/5NV6Rdv1a3I/hqdefault.jpg",
            youtubeVideoId: "5NV6Rdv1a3I",
            genre: "Pop / Synthwave"
        ),

        // Cyberpunk / Synth
        GamaTrack(
            id: "trk_synth_01",
            title: "Turbo Killer",
            artist: "Carpenter Brut",
            album: "Trilogy",
            duration: 247,
            artworkUrl: "https://i.ytimg.com/vi/er416Si74vQ/hqdefault.jpg",
            youtubeVideoId: "er416Si74vQ",
            genre: "Cyberpunk / Synth"
        ),
        GamaTrack(
            id: "trk_synth_02",
            title: "Tech Noir",
            artist: "GUNSHIP",
            album: "GUNSHIP",
            duration: 297,
            artworkUrl: "https://i.ytimg.com/vi/-EDdu3eJ1pY/hqdefault.jpg",
            youtubeVideoId: "-EDdu3eJ1pY",
            genre: "Cyberpunk / Synth"
        ),
        GamaTrack(
            id: "trk_synth_03",
            title: "Resonance",
            artist: "HOME",
            album: "Odyssey",
            duration: 212,
            artworkUrl: "https://i.ytimg.com/vi/8GW6sLrK40k/hqdefault.jpg",
            youtubeVideoId: "8GW6sLrK40k",
            genre: "Cyberpunk / Synth"
        ),
        GamaTrack(
            id: "trk_synth_04",
            title: "Nightcall",
            artist: "Kavinsky",
            album: "OutRun",
            duration: 259,
            artworkUrl: "https://i.ytimg.com/vi/MV_3Dpw-BRY/hqdefault.jpg",
            youtubeVideoId: "MV_3Dpw-BRY",
            genre: "Cyberpunk / Synth"
        ),

        // Hip-Hop
        GamaTrack(
            id: "trk_em_01",
            title: "Lose Yourself",
            artist: "Eminem",
            album: "8 Mile Soundtrack",
            duration: 326,
            artworkUrl: "https://i.ytimg.com/vi/_Yhyp-_hX2s/hqdefault.jpg",
            youtubeVideoId: "_Yhyp-_hX2s",
            genre: "Hip-Hop"
        ),
        GamaTrack(
            id: "trk_ken_01",
            title: "HUMBLE.",
            artist: "Kendrick Lamar",
            album: "DAMN.",
            duration: 177,
            artworkUrl: "https://i.ytimg.com/vi/tvTRZJ-4EyI/hqdefault.jpg",
            youtubeVideoId: "tvTRZJ-4EyI",
            genre: "Hip-Hop"
        ),
        GamaTrack(
            id: "trk_drk_01",
            title: "God's Plan",
            artist: "Drake",
            album: "Scorpion",
            duration: 199,
            artworkUrl: "https://i.ytimg.com/vi/xpVfcZ0ZcFM/hqdefault.jpg",
            youtubeVideoId: "xpVfcZ0ZcFM",
            genre: "Hip-Hop"
        ),
        GamaTrack(
            id: "trk_post_01",
            title: "Circles",
            artist: "Post Malone",
            album: "Hollywood's Bleeding",
            duration: 215,
            artworkUrl: "https://i.ytimg.com/vi/wXhTHyIgQ_U/hqdefault.jpg",
            youtubeVideoId: "wXhTHyIgQ_U",
            genre: "Hip-Hop"
        ),

        // Focus & Ambient
        GamaTrack(
            id: "trk_foc_01",
            title: "Weightless",
            artist: "Marconi Union",
            album: "Weightless (Ambient Transmissions)",
            duration: 485,
            artworkUrl: "https://i.ytimg.com/vi/UfcAVejslrU/hqdefault.jpg",
            youtubeVideoId: "UfcAVejslrU",
            genre: "Focus & Ambient"
        ),
        GamaTrack(
            id: "trk_foc_02",
            title: "432Hz Deep Focus Alpha Wave",
            artist: "Gamma Sound Labs",
            album: "Binaural Harmonic Sessions",
            duration: 360,
            artworkUrl: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
            youtubeVideoId: "1ZYbU82GVz4",
            genre: "Focus & Ambient"
        ),
        GamaTrack(
            id: "trk_foc_03",
            title: "Cyberpunk Ambient City Rain",
            artist: "Neon Matrix",
            album: "Tokyo Rain 2099",
            duration: 420,
            artworkUrl: "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
            youtubeVideoId: "5qap5aO4i9A",
            genre: "Focus & Ambient"
        ),
        GamaTrack(
            id: "trk_foc_04",
            title: "Deep Space Quantum Drift",
            artist: "Vhuwon Mathers",
            album: "Cosmic Resonance",
            duration: 380,
            artworkUrl: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
            youtubeVideoId: "jfKfPfyJRdk",
            genre: "Focus & Ambient"
        )
    ]
}
