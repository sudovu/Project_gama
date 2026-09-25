package com.example

import com.example.core.media.SmartQueueEngine
import com.example.data.provider.CuratedFrequencies
import com.example.data.provider.DailyTop100Registry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChartAndSongCleanTest {

    @Test
    fun testRejectsFakeCompilationsAndMixTitles() {
        val fakeTitles = listOf(
            "Today's Top Hits 2026 - Best New Songs",
            "Billboard 2026 Top Hits Music 2026",
            "Top 100 songs of the day 2026",
            "Pop Christmas Music",
            "New Viral Song 2026",
            "Best songs of 2025 mix",
            "Chill Lofi 24/7 radio live stream",
            "Non Stop Party Hits Jukebox",
            "Greatest Hits Compilation Vol. 1",
            "Full Album Official Audio Mix"
        )

        for (title in fakeTitles) {
            assertTrue("Expected '$title' to be flagged as collection or mix",
                SmartQueueEngine.isCollectionOrMix(title, 210))
        }
    }

    @Test
    fun testAcceptsRealSingleSongTitles() {
        val validSongs = listOf(
            "Die With A Smile",
            "Espresso",
            "Birds of a Feather",
            "Not Like Us",
            "A Bar Song (Tipsy)",
            "Lose Control",
            "Beautiful Things",
            "Good Luck, Babe!",
            "Too Sweet",
            "The Emptiness Machine",
            "Houdini",
            "Blinding Lights",
            "In The End",
            "Numb",
            "Bohemian Rhapsody"
        )

        for (song in validSongs) {
            assertFalse("Expected '$song' to be accepted as a single track",
                SmartQueueEngine.isCollectionOrMix(song, 210))
        }
    }

    @Test
    fun testCleanSongTitleAndArtistExtraction() {
        // Case 1: Standard "Artist - Title (Official Video)"
        val (title1, artist1) = SmartQueueEngine.cleanSongTitleAndArtist(
            "Sabrina Carpenter - Espresso (Official Video)",
            "Sabrina Carpenter"
        )
        assertEquals("Espresso", title1)
        assertEquals("Sabrina Carpenter", artist1)

        // Case 2: Duo "Artist 1, Artist 2 - Title (Official Music Video)"
        val (title2, artist2) = SmartQueueEngine.cleanSongTitleAndArtist(
            "Lady Gaga, Bruno Mars - Die With A Smile (Official Music Video)",
            "LadyGagaVEVO"
        )
        assertEquals("Die With A Smile", title2)
        assertEquals("Lady Gaga, Bruno Mars", artist2)

        // Case 3: Title with bracket badges
        val (title3, artist3) = SmartQueueEngine.cleanSongTitleAndArtist(
            "Eminem - Houdini [Official Music Video]",
            "EminemMusic"
        )
        assertEquals("Houdini", title3)
        assertEquals("Eminem", artist3)

        // Case 4: Title without separator
        val (title4, artist4) = SmartQueueEngine.cleanSongTitleAndArtist(
            "Birds of a Feather (Official Audio)",
            "Billie Eilish - Topic"
        )
        assertEquals("Birds of a Feather", title4)
        assertEquals("Billie Eilish", artist4)
    }

    @Test
    fun testDailyTop100RegistryIntegrity() {
        val chart = DailyTop100Registry.getDailyTop100()
        assertEquals(100, chart.size)

        // Verify ranks 1 to 100
        for (i in 0 until 100) {
            assertEquals(i + 1, chart[i].dailyRank)
            assertTrue("Song title should be clean: ${chart[i].title}",
                !SmartQueueEngine.isCollectionOrMix(chart[i].title, chart[i].durationSeconds))
            assertTrue(chart[i].artist.isNotBlank())
            assertTrue(chart[i].youtubeVideoId.isNotBlank())
            assertTrue(chart[i].chartTrend.isNotBlank())
        }

        assertEquals("HOT #1 OF THE DAY", chart[0].chartTrend)
        assertEquals("TOP 2 RUNNER", chart[1].chartTrend)
        assertEquals("TOP 3 PEAK", chart[2].chartTrend)
    }

    @Test
    fun testPlaylistsRotateOnPullRefresh() {
        val initial = CuratedFrequencies.playlists
        assertTrue("Should have multiple playlists available", initial.size >= 10)

        val rotated1 = CuratedFrequencies.getRotatingPlaylists(12345L)
        val rotated2 = CuratedFrequencies.getRotatingPlaylists(67890L)

        assertEquals(initial.size, rotated1.size)
        // With high probability across 14 items, order is different
        assertNotEquals(rotated1.map { it.id }, rotated2.map { it.id })
    }
}
