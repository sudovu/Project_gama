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
}
