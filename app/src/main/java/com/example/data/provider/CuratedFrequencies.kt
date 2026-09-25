package com.example.data.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Playlist
import com.example.domain.model.Track

object CuratedFrequencies {

    val artists: List<Artist> = listOf(
        Artist(
            id = "art_eminem",
            name = "Eminem",
            bio = "Marshall Bruce Mathers III, known professionally as Eminem. Iconic rapper, songwriter, and record producer credited with popularizing hip hop worldwide.",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Hip-Hop", "Rap", "Hardcore Hip Hop"),
            monthlyListeners = "68.4M Monthly Listeners"
        ),
        Artist(
            id = "art_slipknot",
            name = "Slipknot",
            bio = "Legendary American heavy metal band known for their aggressive, chaotic musical style, energetic live shows, and masked stage personas.",
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Heavy Metal", "Nu Metal", "Alternative Metal"),
            monthlyListeners = "12.8M Monthly Listeners"
        ),
        Artist(
            id = "art_linkinpark",
            name = "Linkin Park",
            bio = "Groundbreaking rock band seamlessly merging alternative rock, nu-metal, and electronic elements, fronted by Chester Bennington and Mike Shinoda.",
            artworkUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Alternative Rock", "Nu Metal", "Electronic Rock"),
            monthlyListeners = "42.1M Monthly Listeners"
        ),
        Artist(
            id = "art_weeknd",
            name = "The Weeknd",
            bio = "Abel Makkonen Tesfaye, known professionally as The Weeknd. Canadian singer-songwriter known for sonic innovation and genre-defining synth-pop and R&B.",
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("R&B", "Synth-Pop", "Pop"),
            monthlyListeners = "105.2M Monthly Listeners"
        ),
        Artist(
            id = "art_billie",
            name = "Billie Eilish",
            bio = "Multi-Grammy and Academy Award-winning visionary singer-songwriter known for whisper-soft vocals and moody, genre-bending dark pop.",
            artworkUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Alt-Pop", "Dark Pop", "Electropop"),
            monthlyListeners = "98.7M Monthly Listeners"
        ),
        Artist(
            id = "art_queen",
            name = "Queen",
            bio = "British rock band formed in London in 1970 by Freddie Mercury, Brian May, Roger Taylor, and John Deacon. Universally celebrated as rock royalty.",
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Classic Rock", "Glam Rock", "Hard Rock"),
            monthlyListeners = "51.3M Monthly Listeners"
        ),
        Artist(
            id = "art_nirvana",
            name = "Nirvana",
            bio = "Pioneering Seattle grunge band formed by Kurt Cobain and Krist Novoselic, whose 1991 anthem transformed the global music landscape.",
            artworkUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Grunge", "Alternative Rock", "Punk"),
            monthlyListeners = "32.0M Monthly Listeners"
        ),
        Artist(
            id = "art_mj",
            name = "Michael Jackson",
            bio = "The King of Pop. One of the most significant cultural figures of the 20th century and the most awarded music artist in history.",
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            headerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&auto=format&fit=crop&q=80",
            genres = listOf("Pop", "R&B", "Disco", "Soul"),
            monthlyListeners = "44.6M Monthly Listeners"
        )
    )

    val allTracks: List<Track> = listOf(
        // Eminem
        Track(
            id = "trk_em_01",
            title = "Lose Yourself",
            artist = "Eminem",
            artistId = "art_eminem",
            albumTitle = "Curtain Call: The Hits",
            albumId = "alb_curtain_call",
            durationSeconds = 326,
            artworkUrl = "https://img.youtube.com/vi/_Yhyp-_hX2s/hqdefault.jpg",
            youtubeVideoId = "_Yhyp-_hX2s",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 1850000000L
        ),
        Track(
            id = "trk_em_02",
            title = "Mockingbird",
            artist = "Eminem",
            artistId = "art_eminem",
            albumTitle = "Curtain Call: The Hits",
            albumId = "alb_curtain_call",
            durationSeconds = 257,
            artworkUrl = "https://img.youtube.com/vi/S9bCLPwzSC0/hqdefault.jpg",
            youtubeVideoId = "S9bCLPwzSC0",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 1200000000L
        ),

        // Slipknot
        Track(
            id = "trk_sk_01",
            title = "Duality",
            artist = "Slipknot",
            artistId = "art_slipknot",
            albumTitle = "Vol. 3: (The Subliminal Verses)",
            albumId = "alb_subliminal",
            durationSeconds = 273,
            artworkUrl = "https://img.youtube.com/vi/6fVE8kSM43I/hqdefault.jpg",
            youtubeVideoId = "6fVE8kSM43I",
            streamUrl = "",
            genre = "Heavy Metal",
            frequencyHz = 432,
            playCount = 410000000L
        ),
        Track(
            id = "trk_sk_02",
            title = "Psychosocial",
            artist = "Slipknot",
            artistId = "art_slipknot",
            albumTitle = "All Hope Is Gone",
            albumId = "alb_subliminal",
            durationSeconds = 304,
            artworkUrl = "https://img.youtube.com/vi/5abamRO41fE/hqdefault.jpg",
            youtubeVideoId = "5abamRO41fE",
            streamUrl = "",
            genre = "Heavy Metal",
            frequencyHz = 432,
            playCount = 520000000L
        ),
        Track(
            id = "trk_sk_03",
            title = "The Devil In I",
            artist = "Slipknot",
            artistId = "art_slipknot",
            albumTitle = ".5: The Gray Chapter",
            albumId = "alb_subliminal",
            durationSeconds = 350,
            artworkUrl = "https://img.youtube.com/vi/XEEasR7hVhA/hqdefault.jpg",
            youtubeVideoId = "XEEasR7hVhA",
            streamUrl = "",
            genre = "Heavy Metal",
            frequencyHz = 432,
            playCount = 380000000L
        ),

        // Linkin Park
        Track(
            id = "trk_lp_01",
            title = "In The End",
            artist = "Linkin Park",
            artistId = "art_linkinpark",
            albumTitle = "Hybrid Theory",
            albumId = "alb_hybrid_theory",
            durationSeconds = 219,
            artworkUrl = "https://img.youtube.com/vi/eVTXPUF4Oz4/hqdefault.jpg",
            youtubeVideoId = "eVTXPUF4Oz4",
            streamUrl = "",
            genre = "Alternative Rock",
            frequencyHz = 432,
            playCount = 1750000000L
        ),
        Track(
            id = "trk_lp_02",
            title = "Numb",
            artist = "Linkin Park",
            artistId = "art_linkinpark",
            albumTitle = "Meteora",
            albumId = "alb_hybrid_theory",
            durationSeconds = 187,
            artworkUrl = "https://img.youtube.com/vi/kXYiU_JCYtU/hqdefault.jpg",
            youtubeVideoId = "kXYiU_JCYtU",
            streamUrl = "",
            genre = "Alternative Rock",
            frequencyHz = 432,
            playCount = 2200000000L
        ),

        // The Weeknd
        Track(
            id = "trk_wk_01",
            title = "Blinding Lights",
            artist = "The Weeknd",
            artistId = "art_weeknd",
            albumTitle = "After Hours",
            albumId = "alb_after_hours",
            durationSeconds = 263,
            artworkUrl = "https://img.youtube.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            youtubeVideoId = "4NRXx6U8ABQ",
            streamUrl = "",
            genre = "Synth-Pop",
            frequencyHz = 432,
            playCount = 4200000000L
        ),

        // Billie Eilish
        Track(
            id = "trk_be_01",
            title = "bad guy",
            artist = "Billie Eilish",
            artistId = "art_billie",
            albumTitle = "WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?",
            albumId = "alb_fall_asleep",
            durationSeconds = 205,
            artworkUrl = "https://img.youtube.com/vi/DyDfgMOUjCI/hqdefault.jpg",
            youtubeVideoId = "DyDfgMOUjCI",
            streamUrl = "",
            genre = "Alt-Pop",
            frequencyHz = 432,
            playCount = 2400000000L
        ),

        // Queen
        Track(
            id = "trk_qn_01",
            title = "Bohemian Rhapsody",
            artist = "Queen",
            artistId = "art_queen",
            albumTitle = "A Night at the Opera",
            albumId = "alb_night_opera",
            durationSeconds = 359,
            artworkUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
            youtubeVideoId = "fJ9rUzIMcZQ",
            streamUrl = "",
            genre = "Classic Rock",
            frequencyHz = 432,
            playCount = 1800000000L
        ),

        // Nirvana
        Track(
            id = "trk_nv_01",
            title = "Smells Like Teen Spirit",
            artist = "Nirvana",
            artistId = "art_nirvana",
            albumTitle = "Nevermind",
            albumId = "alb_nevermind",
            durationSeconds = 278,
            artworkUrl = "https://img.youtube.com/vi/hTWKbfoikeg/hqdefault.jpg",
            youtubeVideoId = "hTWKbfoikeg",
            streamUrl = "",
            genre = "Grunge Rock",
            frequencyHz = 432,
            playCount = 1950000000L
        ),

        // Michael Jackson
        Track(
            id = "trk_mj_01",
            title = "Billie Jean",
            artist = "Michael Jackson",
            artistId = "art_mj",
            albumTitle = "Thriller",
            albumId = "alb_thriller",
            durationSeconds = 295,
            artworkUrl = "https://img.youtube.com/vi/Zi_XLOBDo_Y/hqdefault.jpg",
            youtubeVideoId = "Zi_XLOBDo_Y",
            streamUrl = "",
            genre = "Pop / R&B",
            frequencyHz = 432,
            playCount = 1550000000L
        ),

        // Imagine Dragons
        Track(
            id = "trk_id_01",
            title = "Believer",
            artist = "Imagine Dragons",
            artistId = "art_linkinpark",
            albumTitle = "Evolve",
            albumId = "alb_hybrid_theory",
            durationSeconds = 216,
            artworkUrl = "https://img.youtube.com/vi/7wtfhZwyrcc/hqdefault.jpg",
            youtubeVideoId = "7wtfhZwyrcc",
            streamUrl = "",
            genre = "Pop Rock",
            frequencyHz = 432,
            playCount = 2600000000L
        ),

        // Ed Sheeran
        Track(
            id = "trk_es_01",
            title = "Shape of You",
            artist = "Ed Sheeran",
            artistId = "art_weeknd",
            albumTitle = "Divide",
            albumId = "alb_after_hours",
            durationSeconds = 235,
            artworkUrl = "https://img.youtube.com/vi/JGwWNGJdvx8/hqdefault.jpg",
            youtubeVideoId = "JGwWNGJdvx8",
            streamUrl = "",
            genre = "Pop",
            frequencyHz = 432,
            playCount = 6100000000L
        ),

        // Coldplay
        Track(
            id = "trk_cp_01",
            title = "Viva La Vida",
            artist = "Coldplay",
            artistId = "art_queen",
            albumTitle = "Viva La Vida",
            albumId = "alb_night_opera",
            durationSeconds = 242,
            artworkUrl = "https://img.youtube.com/vi/dvgZkm1xWPE/hqdefault.jpg",
            youtubeVideoId = "dvgZkm1xWPE",
            streamUrl = "",
            genre = "Alternative Pop",
            frequencyHz = 432,
            playCount = 1000000000L
        ),

        // Taylor Swift
        Track(
            id = "trk_ts_01",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            artistId = "art_billie",
            albumTitle = "Lover",
            albumId = "alb_fall_asleep",
            durationSeconds = 178,
            artworkUrl = "https://img.youtube.com/vi/ic8j13piAhQ/hqdefault.jpg",
            youtubeVideoId = "ic8j13piAhQ",
            streamUrl = "",
            genre = "Pop",
            frequencyHz = 432,
            playCount = 1400000000L
        ),

        // Bruno Mars
        Track(
            id = "trk_bm_01",
            title = "24K Magic",
            artist = "Bruno Mars",
            artistId = "art_mj",
            albumTitle = "24K Magic",
            albumId = "alb_thriller",
            durationSeconds = 226,
            artworkUrl = "https://img.youtube.com/vi/UqyT8IEBkvY/hqdefault.jpg",
            youtubeVideoId = "UqyT8IEBkvY",
            streamUrl = "",
            genre = "Funk / Pop",
            frequencyHz = 432,
            playCount = 1600000000L
        ),

        // Adele
        Track(
            id = "trk_ad_01",
            title = "Rolling in the Deep",
            artist = "Adele",
            artistId = "art_billie",
            albumTitle = "21",
            albumId = "alb_fall_asleep",
            durationSeconds = 228,
            artworkUrl = "https://img.youtube.com/vi/rYEDA3JcQqw/hqdefault.jpg",
            youtubeVideoId = "rYEDA3JcQqw",
            streamUrl = "",
            genre = "Soul / Pop",
            frequencyHz = 432,
            playCount = 2300000000L
        ),

        // Metallica
        Track(
            id = "trk_mt_01",
            title = "Enter Sandman",
            artist = "Metallica",
            artistId = "art_slipknot",
            albumTitle = "Metallica (The Black Album)",
            albumId = "alb_subliminal",
            durationSeconds = 331,
            artworkUrl = "https://img.youtube.com/vi/CD-E-LDc384/hqdefault.jpg",
            youtubeVideoId = "CD-E-LDc384",
            streamUrl = "",
            genre = "Heavy Metal",
            frequencyHz = 432,
            playCount = 710000000L
        ),

        // AC/DC
        Track(
            id = "trk_ac_01",
            title = "Back In Black",
            artist = "AC/DC",
            artistId = "art_queen",
            albumTitle = "Back In Black",
            albumId = "alb_night_opera",
            durationSeconds = 255,
            artworkUrl = "https://img.youtube.com/vi/pAgnJDJN4VA/hqdefault.jpg",
            youtubeVideoId = "pAgnJDJN4VA",
            streamUrl = "",
            genre = "Hard Rock",
            frequencyHz = 432,
            playCount = 1100000000L
        ),

        // 50 Cent
        Track(
            id = "trk_50_01",
            title = "In Da Club",
            artist = "50 Cent",
            artistId = "art_eminem",
            albumTitle = "Get Rich or Die Tryin'",
            albumId = "alb_curtain_call",
            durationSeconds = 253,
            artworkUrl = "https://img.youtube.com/vi/5qm8PH4xAss/hqdefault.jpg",
            youtubeVideoId = "5qm8PH4xAss",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 2000000000L
        ),

        // Dr. Dre
        Track(
            id = "trk_dr_01",
            title = "Still D.R.E. (ft. Snoop Dogg)",
            artist = "Dr. Dre",
            artistId = "art_eminem",
            albumTitle = "2001",
            albumId = "alb_curtain_call",
            durationSeconds = 292,
            artworkUrl = "https://img.youtube.com/vi/_CL6n0FJZpk/hqdefault.jpg",
            youtubeVideoId = "_CL6n0FJZpk",
            streamUrl = "",
            genre = "West Coast Rap",
            frequencyHz = 432,
            playCount = 1450000000L
        ),
        Track(
            id = "trk_kl_01",
            title = "HUMBLE.",
            artist = "Kendrick Lamar",
            artistId = "art_eminem",
            albumTitle = "DAMN.",
            albumId = "alb_curtain_call",
            durationSeconds = 184,
            artworkUrl = "https://img.youtube.com/vi/tvTRZJ-4EyI/hqdefault.jpg",
            youtubeVideoId = "tvTRZJ-4EyI",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 1200000000L
        ),
        Track(
            id = "trk_bd_01",
            title = "Mercy",
            artist = "Badshah",
            artistId = "art_eminem",
            albumTitle = "O.N.E.",
            albumId = "alb_curtain_call",
            durationSeconds = 162,
            artworkUrl = "https://img.youtube.com/vi/Jyst8oIHOAY/hqdefault.jpg",
            youtubeVideoId = "Jyst8oIHOAY",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 350000000L
        ),
        Track(
            id = "trk_ts_02",
            title = "SICKO MODE",
            artist = "Travis Scott",
            artistId = "art_eminem",
            albumTitle = "ASTROWORLD",
            albumId = "alb_curtain_call",
            durationSeconds = 312,
            artworkUrl = "https://img.youtube.com/vi/6ONRf7h3Mdk/hqdefault.jpg",
            youtubeVideoId = "6ONRf7h3Mdk",
            streamUrl = "",
            genre = "Hip-Hop",
            frequencyHz = 432,
            playCount = 1400000000L
        ),
        Track(
            id = "trk_soad_01",
            title = "Toxicity",
            artist = "System Of A Down",
            artistId = "art_slipknot",
            albumTitle = "Toxicity",
            albumId = "alb_subliminal",
            durationSeconds = 219,
            artworkUrl = "https://img.youtube.com/vi/iywaBOMvYLI/hqdefault.jpg",
            youtubeVideoId = "iywaBOMvYLI",
            streamUrl = "",
            genre = "Nu Metal",
            frequencyHz = 432,
            playCount = 950000000L
        ),
        Track(
            id = "trk_ev_01",
            title = "Bring Me To Life",
            artist = "Evanescence",
            artistId = "art_linkinpark",
            albumTitle = "Fallen",
            albumId = "alb_hybrid_theory",
            durationSeconds = 254,
            artworkUrl = "https://img.youtube.com/vi/3YxaaGgTQYM/hqdefault.jpg",
            youtubeVideoId = "3YxaaGgTQYM",
            streamUrl = "",
            genre = "Alternative Rock",
            frequencyHz = 432,
            playCount = 1350000000L
        ),
        Track(
            id = "trk_dl_01",
            title = "Levitating",
            artist = "Dua Lipa",
            artistId = "art_billie",
            albumTitle = "Future Nostalgia",
            albumId = "alb_fall_asleep",
            durationSeconds = 203,
            artworkUrl = "https://img.youtube.com/vi/TUVcZfQe-Kw/hqdefault.jpg",
            youtubeVideoId = "TUVcZfQe-Kw",
            streamUrl = "",
            genre = "Pop Hits",
            frequencyHz = 432,
            playCount = 1100000000L
        ),
        Track(
            id = "trk_wk_02",
            title = "Starboy (ft. Daft Punk)",
            artist = "The Weeknd",
            artistId = "art_weeknd",
            albumTitle = "Starboy",
            albumId = "alb_after_hours",
            durationSeconds = 230,
            artworkUrl = "https://img.youtube.com/vi/dqt8Z1k0oWQ/hqdefault.jpg",
            youtubeVideoId = "dqt8Z1k0oWQ",
            streamUrl = "",
            genre = "Synth-Pop",
            frequencyHz = 432,
            playCount = 2800000000L
        ),
        Track(
            id = "trk_qn_02",
            title = "Don't Stop Me Now",
            artist = "Queen",
            artistId = "art_queen",
            albumTitle = "Jazz",
            albumId = "alb_night_opera",
            durationSeconds = 210,
            artworkUrl = "https://img.youtube.com/vi/HgzGwKwLmgM/hqdefault.jpg",
            youtubeVideoId = "HgzGwKwLmgM",
            streamUrl = "",
            genre = "Classic Rock",
            frequencyHz = 432,
            playCount = 1200000000L
        ),
        Track(
            id = "trk_ac_02",
            title = "Highway to Hell",
            artist = "AC/DC",
            artistId = "art_queen",
            albumTitle = "Highway to Hell",
            albumId = "alb_night_opera",
            durationSeconds = 208,
            artworkUrl = "https://img.youtube.com/vi/l482T0yNkeo/hqdefault.jpg",
            youtubeVideoId = "l482T0yNkeo",
            streamUrl = "",
            genre = "Classic Rock",
            frequencyHz = 432,
            playCount = 1400000000L
        ),
        Track(
            id = "trk_mj_02",
            title = "Beat It",
            artist = "Michael Jackson",
            artistId = "art_mj",
            albumTitle = "Thriller",
            albumId = "alb_thriller",
            durationSeconds = 258,
            artworkUrl = "https://img.youtube.com/vi/oRdxUFDoQe0/hqdefault.jpg",
            youtubeVideoId = "oRdxUFDoQe0",
            streamUrl = "",
            genre = "Classics",
            frequencyHz = 432,
            playCount = 1100000000L
        )
    )

    val albums: List<Album> = listOf(
        Album(
            id = "alb_curtain_call",
            title = "Curtain Call: The Hits",
            artist = "Eminem",
            artistId = "art_eminem",
            artworkUrl = "https://img.youtube.com/vi/_Yhyp-_hX2s/hqdefault.jpg",
            releaseYear = "2005",
            genre = "Hip-Hop",
            trackCount = 4,
            tracks = allTracks.filter { it.albumId == "alb_curtain_call" }
        ),
        Album(
            id = "alb_subliminal",
            title = "Vol. 3: (The Subliminal Verses)",
            artist = "Slipknot",
            artistId = "art_slipknot",
            artworkUrl = "https://img.youtube.com/vi/6fVE8kSM43I/hqdefault.jpg",
            releaseYear = "2004",
            genre = "Heavy Metal",
            trackCount = 4,
            tracks = allTracks.filter { it.albumId == "alb_subliminal" }
        ),
        Album(
            id = "alb_hybrid_theory",
            title = "Hybrid Theory",
            artist = "Linkin Park",
            artistId = "art_linkinpark",
            artworkUrl = "https://img.youtube.com/vi/eVTXPUF4Oz4/hqdefault.jpg",
            releaseYear = "2000",
            genre = "Alternative Rock",
            trackCount = 3,
            tracks = allTracks.filter { it.albumId == "alb_hybrid_theory" }
        ),
        Album(
            id = "alb_after_hours",
            title = "After Hours",
            artist = "The Weeknd",
            artistId = "art_weeknd",
            artworkUrl = "https://img.youtube.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            releaseYear = "2020",
            genre = "Synth-Pop",
            trackCount = 2,
            tracks = allTracks.filter { it.albumId == "alb_after_hours" }
        ),
        Album(
            id = "alb_fall_asleep",
            title = "WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?",
            artist = "Billie Eilish",
            artistId = "art_billie",
            artworkUrl = "https://img.youtube.com/vi/DyDfgMOUjCI/hqdefault.jpg",
            releaseYear = "2019",
            genre = "Alt-Pop",
            trackCount = 3,
            tracks = allTracks.filter { it.albumId == "alb_fall_asleep" }
        ),
        Album(
            id = "alb_night_opera",
            title = "A Night at the Opera",
            artist = "Queen",
            artistId = "art_queen",
            artworkUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
            releaseYear = "1975",
            genre = "Classic Rock",
            trackCount = 3,
            tracks = allTracks.filter { it.albumId == "alb_night_opera" }
        ),
        Album(
            id = "alb_thriller",
            title = "Thriller",
            artist = "Michael Jackson",
            artistId = "art_mj",
            artworkUrl = "https://img.youtube.com/vi/Zi_XLOBDo_Y/hqdefault.jpg",
            releaseYear = "1982",
            genre = "Pop / R&B",
            trackCount = 2,
            tracks = allTracks.filter { it.albumId == "alb_thriller" }
        ),
        Album(
            id = "alb_nevermind",
            title = "Nevermind",
            artist = "Nirvana",
            artistId = "art_nirvana",
            artworkUrl = "https://img.youtube.com/vi/hTWKbfoikeg/hqdefault.jpg",
            releaseYear = "1991",
            genre = "Grunge",
            trackCount = 1,
            tracks = allTracks.filter { it.albumId == "alb_nevermind" }
        )
    )

    val playlists: List<Playlist> = listOf(
        Playlist(
            id = "pl_global_hits",
            title = "Global Hot Hits 2026",
            description = "The biggest chart-topping hits with millions of fans worldwide.",
            artworkUrl = "https://img.youtube.com/vi/b_pD_J86NfQ/hqdefault.jpg",
            trackCount = allTracks.size,
            isUserCreated = false,
            tracks = allTracks
        ),
        Playlist(
            id = "pl_rock_metal",
            title = "Rock & Metal Anthems",
            description = "Raw guitar energy, thunderous drums, and iconic voices from Slipknot, Linkin Park, Metallica, and AC/DC.",
            artworkUrl = "https://img.youtube.com/vi/6fVE8kSM43I/hqdefault.jpg",
            trackCount = 8,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Rock") || it.genre.contains("Metal") || it.genre.contains("Grunge") }
        ),
        Playlist(
            id = "pl_rap_legends",
            title = "Hip-Hop & Rap Titans",
            description = "Legendary bars, beats, and lyrical mastery from Eminem, Kendrick Lamar, and Travis Scott.",
            artworkUrl = "https://img.youtube.com/vi/S9bCLPwzSC0/hqdefault.jpg",
            trackCount = 8,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Rap") || it.genre.contains("Hip-Hop") }
        ),
        Playlist(
            id = "pl_pop_electric",
            title = "Iconic Pop & Electropop",
            description = "Irresistible melodies and production from Sabrina Carpenter, Lady Gaga, The Weeknd, and Billie Eilish.",
            artworkUrl = "https://img.youtube.com/vi/eVli-tstM5E/hqdefault.jpg",
            trackCount = 10,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Pop") || it.genre.contains("Soul") || it.genre.contains("Funk") }
        ),
        Playlist(
            id = "pl_cyberpunk",
            title = "Cyberpunk & Synthwave",
            description = "Neon nocturnal synths, analog basslines, and retro-futuristic audio architecture.",
            artworkUrl = "https://img.youtube.com/vi/4NRXx6U8ABQ/hqdefault.jpg",
            trackCount = 6,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Synth") || it.genre.contains("Cyberpunk") || it.genre.contains("Electronic") }
        ),
        Playlist(
            id = "pl_nu_metal",
            title = "Nu Metal & Heavy Alt-Rock",
            description = "Drop tunings, crushing breakdowns, and aggressive energy from Linkin Park and Slipknot.",
            artworkUrl = "https://img.youtube.com/vi/eVTXPUF4Oz4/hqdefault.jpg",
            trackCount = 7,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Nu Metal") || it.genre.contains("Alternative Rock") || it.genre.contains("Heavy Metal") }
        ),
        Playlist(
            id = "pl_edm",
            title = "Festival EDM & Electro",
            description = "Massive drops, euphoric builds, and high-energy festival anthems.",
            artworkUrl = "https://img.youtube.com/vi/dX3k_QDnzHE/hqdefault.jpg",
            trackCount = 6,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("EDM") || it.genre.contains("Electronic") || it.genre.contains("Synth-Pop") }
        ),
        Playlist(
            id = "pl_432hz",
            title = "432Hz Natural Healing Tone",
            description = "Harmonically tuned soundscapes resonated at 432Hz for deep cellular calm and equilibrium.",
            artworkUrl = "https://img.youtube.com/vi/DyDfgMOUjCI/hqdefault.jpg",
            trackCount = 12,
            isUserCreated = false,
            tracks = allTracks.filter { it.frequencyHz == 432 }
        ),
        Playlist(
            id = "pl_lofi",
            title = "Lo-Fi Beats to Relax & Study",
            description = "Cozy vinyl crackle, tape-saturated keys, and chill instrumental rhythms.",
            artworkUrl = "https://img.youtube.com/vi/p8782Z_V7lE/hqdefault.jpg",
            trackCount = 8,
            isUserCreated = false,
            tracks = allTracks
        ),
        Playlist(
            id = "pl_acoustic",
            title = "Acoustic Sunsets & Indie Folk",
            description = "Warm acoustic guitars, intimate storytelling, and honest campfire vocals.",
            artworkUrl = "https://img.youtube.com/vi/t7bQwwqW-Hc/hqdefault.jpg",
            trackCount = 6,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Acoustic") || it.genre.contains("Folk") }
        ),
        Playlist(
            id = "pl_rnb",
            title = "Midnight R&B & Neo-Soul",
            description = "Silky basslines, velvet vocals, and late night atmospheric grooves.",
            artworkUrl = "https://img.youtube.com/vi/95Wc7eI3s6c/hqdefault.jpg",
            trackCount = 7,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("R&B") || it.genre.contains("Soul") }
        ),
        Playlist(
            id = "pl_phonk",
            title = "Night Drift Phonk & Bass",
            description = "Distorted 808s, cowbell melodies, and street racing drive energy.",
            artworkUrl = "https://img.youtube.com/vi/22tVWwmTie8/hqdefault.jpg",
            trackCount = 5,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Hip-Hop") || it.genre.contains("Rap") }
        ),
        Playlist(
            id = "pl_classics",
            title = "Timeless Golden Classics",
            description = "The immortal legendary recordings that shaped modern music history.",
            artworkUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg",
            trackCount = 8,
            isUserCreated = false,
            tracks = allTracks.filter { it.genre.contains("Classic") || it.genre.contains("Rock") }
        ),
        Playlist(
            id = "pl_daily_top",
            title = "Daily Top 100 Chart Spotlight",
            description = "Everyday verified chart leaders, hot climbers, and peak anthems.",
            artworkUrl = "https://img.youtube.com/vi/V9PVRfjEBTI/hqdefault.jpg",
            trackCount = 100,
            isUserCreated = false,
            tracks = allTracks
        )
    )

    fun getRotatingPlaylists(seed: Long = System.currentTimeMillis()): List<Playlist> {
        val rng = java.util.Random(seed)
        return playlists.shuffled(rng)
    }

    fun getRotatingAlbums(seed: Long = System.currentTimeMillis()): List<Album> {
        val rng = java.util.Random(seed)
        return albums.shuffled(rng)
    }

    fun getRotatingArtists(seed: Long = System.currentTimeMillis()): List<Artist> {
        val rng = java.util.Random(seed)
        return artists.shuffled(rng)
    }
}
