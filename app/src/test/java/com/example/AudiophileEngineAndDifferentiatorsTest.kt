package com.example

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.core.util.AudioMetadataExtractor
import com.example.data.provider.LrcLibLyricsProvider
import com.example.domain.model.EqualizerSettings
import com.example.domain.model.Track
import com.example.ui.theme.DynamicPaletteExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AudiophileEngineAndDifferentiatorsTest {

    @Test
    fun testAudioQualityBadgeComputation() {
        // FLAC 24-bit / 96kHz Lossless
        val flacBadge = AudioMetadataExtractor.computeQualityBadge(
            codec = "FLAC",
            bitrateKbps = 2400,
            sampleRateHz = 96000,
            bitDepth = 24
        )
        assertEquals("FLAC • 24-bit/96kHz", flacBadge)

        // WAV 16-bit / 44.1kHz PCM
        val wavBadge = AudioMetadataExtractor.computeQualityBadge(
            codec = "WAV",
            bitrateKbps = 1411,
            sampleRateHz = 44100,
            bitDepth = 16
        )
        assertEquals("WAV • 16-bit/44kHz", wavBadge)

        // High Quality 320 kbps MP3/AAC
        val hqBadge = AudioMetadataExtractor.computeQualityBadge(
            codec = "MP3",
            bitrateKbps = 320,
            sampleRateHz = 44100,
            bitDepth = 16
        )
        assertEquals("HQ AUDIO • 320 kbps", hqBadge)

        // Standard 256 kbps AAC
        val aacBadge = AudioMetadataExtractor.computeQualityBadge(
            codec = "AAC",
            bitrateKbps = 256,
            sampleRateHz = 44100,
            bitDepth = 16
        )
        assertEquals("AAC • 256 kbps", aacBadge)
    }

    @Test
    fun testTrackAudioQualityBadgeProperty() {
        val hiResTrack = Track(
            id = "test_track_1",
            title = "Quantum Echoes",
            artist = "GAMA Acoustic Lab",
            codec = "FLAC",
            bitrateKbps = 2400,
            sampleRateHz = 96000,
            bitDepth = 24
        )
        assertEquals("Hi-Res Lossless • 24bit/96kHz", hiResTrack.audioQualityBadge)

        val losslessTrack = Track(
            id = "test_track_2",
            title = "Acoustic Resonance",
            artist = "GAMA Studio",
            codec = "FLAC",
            bitrateKbps = 900,
            sampleRateHz = 44100,
            bitDepth = 16
        )
        assertEquals("Hi-Res Lossless • 16bit/44kHz", losslessTrack.audioQualityBadge)

        val studioTrack = Track(
            id = "test_track_3",
            title = "Harmonic Pulse",
            artist = "GAMA",
            codec = "AAC",
            bitrateKbps = 320
        )
        assertEquals("AAC • 320kbps", studioTrack.audioQualityBadge)
    }

    @Test
    fun testSrtSubtitleParsing() {
        val srtContent = """
            1
            00:00:02,500 --> 00:00:05,000
            In the beginning of time

            2
            00:00:05,500 --> 00:00:08,200
            Cosmic frequencies aligned
        """.trimIndent()

        val parsed = LrcLibLyricsProvider.parseSrt(srtContent)
        assertEquals(2, parsed.size)
        assertEquals(2500L, parsed[0].timeMs)
        assertEquals("In the beginning of time", parsed[0].text)
        assertEquals(5500L, parsed[1].timeMs)
        assertEquals("Cosmic frequencies aligned", parsed[1].text)
    }

    @Test
    fun testVttSubtitleParsing() {
        val vttContent = """
            WEBVTT

            00:00:01.200 --> 00:00:03.500
            First line of synchronized lyric

            00:00:04.000 --> 00:00:07.100
            Second line of lyric
        """.trimIndent()

        val parsed = LrcLibLyricsProvider.parseVtt(vttContent)
        assertEquals(2, parsed.size)
        assertEquals(1200L, parsed[0].timeMs)
        assertEquals("First line of synchronized lyric", parsed[0].text)
        assertEquals(4000L, parsed[1].timeMs)
        assertEquals("Second line of lyric", parsed[1].text)
    }

    @Test
    fun testLrcParserWithMillisecondPrecision() {
        val lrc = """
            [00:15.30]Welcome to GAMA
            [01:02.85]Audiophile Pure Sound
        """.trimIndent()

        val parsed = LrcLibLyricsProvider.parseLrc(lrc)
        assertEquals(2, parsed.size)
        assertEquals(15300L, parsed[0].timeMs)
        assertEquals("Welcome to GAMA", parsed[0].text)
        assertEquals(62850L, parsed[1].timeMs)
        assertEquals("Audiophile Pure Sound", parsed[1].text)
    }

    @Test
    fun testDynamicPaletteExtractorFiltersVibrantColor() {
        // Create 32x32 bitmap filled with vibrant cyan #00F5D4
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val cyanColor = AndroidColor.rgb(0x00, 0xF5, 0xD4)
        for (x in 0 until 32) {
            for (y in 0 until 32) {
                bmp.setPixel(x, y, cyanColor)
            }
        }

        val extracted = DynamicPaletteExtractor.extractDominantColor(bmp)
        assertNotNull(extracted)
        // Check green and blue channels dominate red
        assertTrue(extracted!!.green > 0.8f)
        assertTrue(extracted.blue > 0.7f)
        assertTrue(extracted.red < 0.2f)
    }

    @Test
    fun testEqualizerSettingsDefaultsAndAudioContinuity() {
        val eq = EqualizerSettings(
            isEnabled = true,
            masterGain = 1.0f,
            preampGainDb = 2.5f,
            isReplayGainEnabled = true,
            crossfadeSeconds = 4
        )

        assertEquals(2.5f, eq.preampGainDb)
        assertTrue(eq.isReplayGainEnabled)
        assertEquals(4, eq.crossfadeSeconds)
    }

    @Test
    fun testWaveformExtractorGenerates100Peaks() {
        val track = Track(
            id = "test_wf_track",
            title = "Aura Borealis",
            artist = "GAMA Resonator",
            durationSeconds = 240
        )
        val waveform = com.example.core.util.WaveformExtractor.generateRealisticWaveform(track)
        assertEquals(100, waveform.size)

        for (peak in waveform) {
            assertTrue("Peak must be >= 0.12f, but was $peak", peak >= 0.12f)
            assertTrue("Peak must be <= 1.0f, but was $peak", peak <= 1.0f)
        }

        // Chorus peak around 40% should exceed intro around 5%
        val introAmp = waveform[5]
        val chorusAmp = waveform[40]
        assertTrue("Chorus amplitude ($chorusAmp) should exceed intro ($introAmp)", chorusAmp > introAmp)
    }

    @Test
    fun testSmartFilterAudiophileShelves() {
        val tracks = listOf(
            Track(id = "1", title = "Lossless FLAC", artist = "A", codec = "FLAC", bitDepth = 24, sampleRateHz = 96000, playCount = 10),
            Track(id = "2", title = "Standard MP3", artist = "B", codec = "MP3", bitrateKbps = 320, playCount = 50),
            Track(id = "3", title = "YouTube Video Track", artist = "C", youtubeVideoId = "dQw4w9WgXcQ", playCount = 5),
            Track(id = "4", title = "WAV Master", artist = "D", codec = "WAV", bitDepth = 16, sampleRateHz = 44100, playCount = 2)
        )

        val hiResFilter = com.example.feature.library.LibraryUiState(selectedSmartFilter = com.example.feature.library.SmartFilter.HI_RES)
        val hiResTracks = hiResFilter.applySmartFilter(tracks)
        assertEquals(2, hiResTracks.size)
        assertTrue(hiResTracks.any { it.id == "1" })
        assertTrue(hiResTracks.any { it.id == "4" })

        val mostPlayedFilter = com.example.feature.library.LibraryUiState(selectedSmartFilter = com.example.feature.library.SmartFilter.MOST_PLAYED)
        val mostPlayedTracks = mostPlayedFilter.applySmartFilter(tracks)
        assertEquals("2", mostPlayedTracks.first().id) // 50 plays

        val videoFilter = com.example.feature.library.LibraryUiState(selectedSmartFilter = com.example.feature.library.SmartFilter.VIDEO_TRACKS)
        val videoTracks = videoFilter.applySmartFilter(tracks)
        assertEquals(1, videoTracks.size)
        assertEquals("3", videoTracks.first().id)
    }

    @Test
    fun testLanSyncServerProtocol() {
        // Test command execution JSON string
        val cmdJson = """{"action":"toggle_play_pause"}"""
        val response = com.example.core.remote.GamaLanSyncServer.executeCommand(cmdJson)
        assertNotNull(response)
        assertTrue(response.contains("toggle_play_pause") || response.contains("playbackManager is null") || response.contains("success"))
    }

    @Test
    fun testLanSyncPinPairingAndTokenValidation() {
        val server = com.example.core.remote.GamaLanSyncServer
        val pin = server.pairingPin
        assertEquals(4, pin.length)
        assertTrue(pin.all { it.isDigit() })

        // Test invalid PIN
        val invalidToken = server.pairWithPin("invalid_pin_99999")
        org.junit.Assert.assertNull(invalidToken)

        // Test valid PIN
        val validToken = server.pairWithPin(pin)
        assertNotNull(validToken)
        assertTrue(validToken!!.isNotBlank())
        assertTrue(server.validateToken(validToken))
        org.junit.Assert.assertFalse(server.validateToken("fake_random_token_xyz"))

        // Test reset PIN revokes active tokens
        val newPin = server.resetPin()
        assertEquals(4, newPin.length)
        org.junit.Assert.assertFalse(server.validateToken(validToken))
    }

    @Test
    fun testWaveformExtractorLruEviction() {
        val tempDir = java.io.File.createTempFile("waveforms_test", "").apply {
            delete()
            mkdirs()
        }
        try {
            // Create 5 fake .bin files with different timestamps
            val files = (1..5).map { i ->
                val f = java.io.File(tempDir, "track_$i.bin")
                f.writeBytes(ByteArray(1024 * 10)) // 10 KB each
                f.setLastModified(1000L * i) // track_1 is oldest, track_5 is newest
                f
            }

            // Total size is 50 KB. Trim with maxBytes = 35 KB, targetBytes = 25 KB
            com.example.core.util.WaveformExtractor.trimDiskCacheIfNeeded(
                dir = tempDir,
                maxBytes = 35 * 1024L,
                targetBytes = 25 * 1024L
            )

            // Oldest files (track_1, track_2, track_3) should be evicted
            org.junit.Assert.assertFalse("track_1 should be evicted", java.io.File(tempDir, "track_1.bin").exists())
            org.junit.Assert.assertFalse("track_2 should be evicted", java.io.File(tempDir, "track_2.bin").exists())
            assertTrue("track_5 should be kept", java.io.File(tempDir, "track_5.bin").exists())
            assertTrue("track_4 should be kept", java.io.File(tempDir, "track_4.bin").exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testLanStreamingAndQueuePushProtocol() {
        val server = com.example.core.remote.GamaLanSyncServer
        val pin = server.pairingPin
        val token = server.pairWithPin(pin)
        assertNotNull(token)

        // Validate token acceptance
        assertTrue(server.validateToken(token))

        // Validate command execution with valid payload
        val pushJson = """{"action":"toggle_shuffle"}"""
        val res = server.executeCommand(pushJson)
        assertNotNull(res)
        assertTrue(res.contains("toggle_shuffle") || res.contains("playbackManager is null") || res.contains("success"))
    }

    @Test
    fun testLanQueueCleanupHygiene() {
        val tempQueueDir = java.io.File.createTempFile("lan_queue_test", "").apply {
            delete()
            mkdirs()
        }
        try {
            // Create 25 mock audio upload files
            (1..25).forEach { i ->
                val f = java.io.File(tempQueueDir, "test_track_$i.mp3")
                f.writeBytes(ByteArray(1024 * 50)) // 50 KB each
                f.setLastModified(1000L * i)
            }
            assertEquals(25, tempQueueDir.listFiles()?.size)

            // Trigger cleanup with maxTotalBytes = 500 KB
            com.example.core.remote.GamaLanSyncServer.cleanupLanQueueDirectory(tempQueueDir, maxTotalBytes = 500 * 1024L)

            // Older files should be pruned, remaining files <= 15
            val remaining = tempQueueDir.listFiles() ?: emptyArray()
            assertTrue("Expected <= 15 remaining files after cleanup, got ${remaining.size}", remaining.size <= 15)
            assertTrue("Newest file should still exist", java.io.File(tempQueueDir, "test_track_25.mp3").exists())
        } finally {
            tempQueueDir.deleteRecursively()
        }
    }

    @Test
    fun testWebCompanionHtmlRendering() {
        val html = com.example.core.remote.GamaLanSyncServer.generateWebCompanionHtml()
        assertNotNull(html)
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("GAMA • Web Remote & LAN Stream"))
        assertTrue(html.contains("canvas id=\"viz\""))
        assertTrue(html.contains("pairWithPin"))
        assertTrue(html.contains("toggleStreamAudio"))
    }

    @Test
    fun testDirectStreamTrackCreation() {
        val streamUrl = "https://somafm.com/live/groove_salad_256.mp3"
        val track = com.example.core.util.StreamPlaylistParser.createDirectStreamTrack(
            url = streamUrl,
            fallbackTitle = "Groove Salad",
            fallbackArtist = "SomaFM"
        )
        assertNotNull(track)
        assertEquals("Groove Salad", track.title)
        assertEquals("SomaFM", track.artist)
        assertEquals(streamUrl, track.streamUrl)
        assertEquals("MP3", track.codec)
        assertEquals("stream", track.source)
    }

    @Test
    fun testM3uContentParsing() {
        val m3uText = """
            #EXTM3U
            #EXTINF:180,Tycho - Awake
            https://stream.example.com/tycho_awake.flac
            #EXTINF:240,Boards of Canada - Dayvan Cowboy
            https://stream.example.com/dayvan_cowboy.ogg
            #EXTINF:-1,Cyberpunk Ambient Radio
            https://radio.example.com/live.m3u8
        """.trimIndent()

        val tracks = com.example.core.util.StreamPlaylistParser.parseM3uContent(m3uText)
        assertEquals(3, tracks.size)

        assertEquals("Awake", tracks[0].title)
        assertEquals("Tycho", tracks[0].artist)
        assertEquals(180, tracks[0].durationSeconds)
        assertEquals("FLAC", tracks[0].codec)

        assertEquals("Dayvan Cowboy", tracks[1].title)
        assertEquals("Boards of Canada", tracks[1].artist)
        assertEquals(240, tracks[1].durationSeconds)
        assertEquals("OGG", tracks[1].codec)

        assertEquals("Cyberpunk Ambient Radio", tracks[2].title)
        assertEquals(0, tracks[2].durationSeconds)
        assertEquals("HLS", tracks[2].codec)
    }

    @Test
    fun testStreamPlaylistDiskCache() {
        val tempCacheDir = java.io.File.createTempFile("stream_cache_test", "").apply {
            delete()
            mkdirs()
        }
        try {
            val tracks = listOf(
                com.example.core.util.StreamPlaylistParser.createDirectStreamTrack(
                    url = "https://radio.example.com/stream1.aac",
                    fallbackTitle = "Synthwave 80s",
                    fallbackArtist = "Cyber Radio"
                ),
                com.example.core.util.StreamPlaylistParser.createDirectStreamTrack(
                    url = "https://radio.example.com/stream2.flac",
                    fallbackTitle = "Lossless Jazz",
                    fallbackArtist = "Hi-Res Stream"
                )
            )

            // Save to cache
            com.example.core.util.StreamPlaylistParser.saveToCache(tempCacheDir, "Cyberpunk Favorites", tracks)

            // Reload from cache
            val loaded = com.example.core.util.StreamPlaylistParser.loadCachedPlaylists(tempCacheDir)
            assertTrue("Expected cached playlist 'Cyberpunk Favorites'", loaded.containsKey("Cyberpunk Favorites"))
            assertEquals(2, loaded["Cyberpunk Favorites"]?.size)
            assertEquals("Synthwave 80s", loaded["Cyberpunk Favorites"]?.get(0)?.title)
            assertEquals("AAC", loaded["Cyberpunk Favorites"]?.get(0)?.codec)
        } finally {
            tempCacheDir.deleteRecursively()
        }
    }

    @Test
    fun testM3uRelativePathResolution() {
        val parser = com.example.core.util.StreamPlaylistParser
        val baseFile = java.io.File("/storage/emulated/0/Music/Playlists/chill.m3u")

        // Relative track in same folder
        val resolvedSame = parser.resolveUrl(baseFile.absolutePath, "ambient_01.flac")
        val expectedSame = java.io.File("/storage/emulated/0/Music/Playlists/ambient_01.flac").absolutePath
        assertEquals(expectedSame, resolvedSame)

        // Relative track in subfolder
        val resolvedSub = parser.resolveUrl(baseFile.absolutePath, "subfolder/ambient_02.wav")
        val expectedSub = java.io.File("/storage/emulated/0/Music/Playlists/subfolder/ambient_02.wav").absolutePath
        assertEquals(expectedSub, resolvedSub)

        // Absolute URL remains unchanged
        val remoteUrl = "https://stream.example.com/live.m3u8"
        val resolvedRemote = parser.resolveUrl(baseFile.absolutePath, remoteUrl)
        assertEquals(remoteUrl, resolvedRemote)
    }

    @Test
    fun testM3uInputStreamParsing() {
        val m3uText = """
            #EXTM3U
            #EXTINF:215,Solaris Pulse - Neon Horizon
            relative_track.mp3
            #EXTINF:0,Cyber Radio Live
            https://live.somafm.com/groovesalad.mp3
        """.trimIndent()

        val inputStream = java.io.ByteArrayInputStream(m3uText.toByteArray(Charsets.UTF_8))
        val tracks = com.example.core.util.StreamPlaylistParser.parseM3uInputStream(
            inputStream = inputStream,
            baseUriOrPath = "/sdcard/Music/playlist.m3u",
            fallbackTitle = "Synthwave Stream",
            fallbackArtist = "Cyber Lab"
        )

        assertEquals(2, tracks.size)
        assertEquals("Neon Horizon", tracks[0].title)
        assertEquals("Solaris Pulse", tracks[0].artist)
        assertEquals(215, tracks[0].durationSeconds)
        assertTrue(tracks[0].streamUrl.endsWith("relative_track.mp3"))

        assertEquals("Cyber Radio Live", tracks[1].title)
        assertEquals("https://live.somafm.com/groovesalad.mp3", tracks[1].streamUrl)
        assertEquals(0, tracks[1].durationSeconds)
        assertTrue(tracks[1].isLiveStream)
    }

    @Test
    fun testLiveStreamDetectionAndTrackProperties() {
        val vodTrack = Track(
            id = "vod_1",
            title = "Finite Song",
            artist = "Artist",
            durationSeconds = 180,
            streamUrl = "https://example.com/audio.mp3"
        )
        assertTrue(!vodTrack.isLiveStream)

        val liveTrack = Track(
            id = "live_1",
            title = "Continuous Radio",
            artist = "Broadcaster",
            durationSeconds = 0,
            streamUrl = "https://example.com/live.aac"
        )
        assertTrue(liveTrack.isLiveStream)

        val hlsTrack = Track(
            id = "hls_1",
            title = "HLS Stream",
            artist = "Broadcaster",
            durationSeconds = 300,
            streamUrl = "https://example.com/stream.m3u8",
            codec = "HLS"
        )
        assertTrue(hlsTrack.isLiveStream)
    }

    @Test
    fun testLiveStreamBackoffCalculation() {
        val pm = com.example.core.media.PlaybackManager
        assertEquals(1000L, pm.computeBackoffMs(1)) // 1s
        assertEquals(2000L, pm.computeBackoffMs(2)) // 2s
        assertEquals(4000L, pm.computeBackoffMs(3)) // 4s
        assertEquals(8000L, pm.computeBackoffMs(4)) // 8s
        assertEquals(8000L, pm.computeBackoffMs(5)) // Capped at 8s
        assertEquals(8000L, pm.computeBackoffMs(10)) // Capped at 8s
    }

    @Test
    fun testStreamSerializationAndDeserialization() {
        val parser = com.example.core.util.StreamPlaylistParser
        val sampleStreams = listOf(
            Track(
                id = "stream_fav_01",
                title = "Cyber Ambient Chill",
                artist = "SomaFM",
                durationSeconds = 0,
                streamUrl = "https://somafm.com/groovesalad130.pls",
                codec = "MP3",
                source = "stream"
            ),
            Track(
                id = "stream_fav_02",
                title = "DEF CON Radio",
                artist = "DEF CON",
                durationSeconds = 0,
                streamUrl = "https://somafm.com/defcon.pls",
                codec = "AAC",
                source = "stream"
            )
        )

        val json = parser.serializeTrackList(sampleStreams)
        assertTrue(json.contains("Cyber Ambient Chill"))
        assertTrue(json.contains("defcon"))

        val deserialized = parser.deserializeTrackList(json)
        assertEquals(2, deserialized.size)
        assertEquals("Cyber Ambient Chill", deserialized[0].title)
        assertEquals("https://somafm.com/groovesalad130.pls", deserialized[0].streamUrl)
        assertEquals("DEF CON Radio", deserialized[1].title)
        assertEquals("https://somafm.com/defcon.pls", deserialized[1].streamUrl)
        assertTrue(deserialized[0].isLiveStream)
        assertTrue(deserialized[1].isLiveStream)
    }
}
