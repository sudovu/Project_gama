package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.media.PlaybackManager
import com.example.data.local.GammaDatabase
import com.example.data.provider.ComplianceMode
import com.example.data.provider.MockMusicProvider
import com.example.data.provider.MusicProvider
import com.example.data.provider.PlaybackCapability
import com.example.data.repository.MusicRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DiscoverFeed
import com.example.domain.model.Playlist
import com.example.domain.model.SearchResults
import com.example.domain.model.Track
import com.example.feature.search.SearchScope
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: GammaDatabase
    private lateinit var repository: MusicRepository
    private lateinit var playbackManager: PlaybackManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GammaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playbackManager = PlaybackManager(context)
        repository = MusicRepository(
            provider = MockMusicProvider(),
            favoriteDao = database.favoriteDao(),
            recentPlaybackDao = database.recentPlaybackDao(),
            playlistDao = database.playlistDao(),
            searchHistoryDao = database.searchHistoryDao(),
            uploadedTrackDao = database.uploadedTrackDao(),
            discoveryCacheDao = database.discoveryCacheDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app name string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("GAMA", appName)
    }

    @Test
    fun `toggle favorite persistence in room database`() = runBlocking {
        val testTrack = Track(
            id = "test_signal_1",
            title = "Orbital Frequency",
            artist = "Aether Resonance",
            durationSeconds = 180,
            frequencyHz = 432
        )

        // Initially not favorite
        assertFalse(repository.isFavorite(testTrack.id).first())

        // Toggle on
        repository.toggleFavorite(testTrack)
        assertTrue(repository.isFavorite(testTrack.id).first())

        val favorites = repository.getFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("Orbital Frequency", favorites.first().title)

        // Toggle off
        repository.toggleFavorite(testTrack)
        assertFalse(repository.isFavorite(testTrack.id).first())
        assertTrue(repository.getFavorites().first().isEmpty())
    }

    @Test
    fun `upload music and ensure it is discoverable across feed and search`() = runBlocking {
        // Upload a custom music track
        val uploaded = repository.uploadTrack(
            title = "Galactic Pulse 432Hz",
            artist = "Aura Synthesizer",
            frequencyHz = 432,
            genre = "Synthwave",
            youtubeUrlOrId = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            localAudioUri = ""
        )

        assertNotNull(uploaded.id)
        assertTrue(uploaded.isUploaded)
        assertEquals("Galactic Pulse 432Hz", uploaded.title)
        assertEquals("dQw4w9WgXcQ", uploaded.youtubeVideoId)

        // Verify discoverable in Discover Feed
        val feedResult = repository.getDiscoverFeed().first().getOrThrow()
        assertTrue(feedResult.uploadedTracks.any { it.id == uploaded.id })

        // Verify discoverable in Search with scope ALL
        val allSearchResults = repository.searchWithScope("Galactic", SearchScope.ALL).first().getOrThrow()
        assertTrue(allSearchResults.uploadedTracks.any { it.id == uploaded.id })

        // Verify discoverable in Search with scope UPLOADED
        val uploadedSearchResults = repository.searchWithScope("Galactic", SearchScope.UPLOADED).first().getOrThrow()
        assertTrue(uploadedSearchResults.uploadedTracks.any { it.id == uploaded.id })

        // Search in YOUTUBE_MUSIC scope isolates music
        val ytMusicSearchResults = repository.searchWithScope("Pulse", SearchScope.YOUTUBE_MUSIC).first().getOrThrow()
        assertEquals(SearchScope.YOUTUBE_MUSIC.name, ytMusicSearchResults.searchScope)
    }

    @Test
    fun `playback manager queue and shuffle operations`() {
        val track1 = Track(id = "trk1", title = "Wave 1", artist = "Artist 1", durationSeconds = 100)
        val track2 = Track(id = "trk2", title = "Wave 2", artist = "Artist 2", durationSeconds = 120)
        val track3 = Track(id = "trk3", title = "Wave 3", artist = "Artist 3", durationSeconds = 140)

        playbackManager.playQueue(listOf(track1, track2, track3), startIndex = 0)

        val state1 = playbackManager.playbackState.value
        assertEquals("trk1", state1.currentTrack?.id)
        assertTrue(state1.isPlaying)
        assertEquals(3, state1.queue.size)

        // Skip Next
        playbackManager.skipNext()
        val state2 = playbackManager.playbackState.value
        assertEquals("trk2", state2.currentTrack?.id)

        // Toggle Shuffle
        playbackManager.toggleShuffle()
        val stateShuffle = playbackManager.playbackState.value
        assertTrue(stateShuffle.isShuffle)
        assertEquals("trk2", stateShuffle.currentTrack?.id)
    }

    @Test
    fun `create and query custom playlist in room database`() = runBlocking {
        val playlistId = repository.createPlaylist("Deep Space Ambient", "Frequency sleep waves")
        assertNotNull(playlistId)

        val playlists = repository.getUserPlaylists().first()
        assertEquals(1, playlists.size)
        assertEquals("Deep Space Ambient", playlists.first().title)

        val track = Track(id = "trk_cosmic", title = "Aurora Flight", artist = "Helios", durationSeconds = 210)
        repository.addTrackToPlaylist(playlistId, track)

        val playlistWithTracks = repository.getPlaylist(playlistId).first().getOrThrow()
        assertEquals(1, playlistWithTracks.tracks.size)
        assertEquals("Aurora Flight", playlistWithTracks.tracks.first().title)
    }

    @Test
    fun `music metadata cached in room during discovery allows offline feed browsing`() = runBlocking {
        // 1. Initial discovery online populates the Room cache
        val onlineFeed = repository.getDiscoverFeed().first().getOrThrow()
        assertTrue(onlineFeed.trendingTracks.isNotEmpty())

        // Verify discovery history in Room is populated
        val cachedTracks = repository.getDiscoveryHistory().first()
        assertTrue(cachedTracks.isNotEmpty())
        assertTrue(cachedTracks.any { it.id == onlineFeed.trendingTracks.first().id })

        // 2. Simulate offline environment where network provider fails
        val failingProvider = object : MusicProvider {
            override val id: String = "offline_failing"
            override val displayName: String = "Offline Failing Provider"
            override suspend fun getDiscoverFeed(): Result<DiscoverFeed> {
                return Result.failure(IOException("No internet connection"))
            }
            override suspend fun search(query: String): Result<SearchResults> {
                return Result.failure(IOException("No internet connection"))
            }
            override suspend fun getTrack(trackId: String): Result<Track> = Result.failure(IOException("Offline"))
            override suspend fun getArtist(artistId: String): Result<Artist> = Result.failure(IOException("Offline"))
            override suspend fun getAlbum(albumId: String): Result<Album> = Result.failure(IOException("Offline"))
            override suspend fun getPlaylist(playlistId: String): Result<Playlist> = Result.failure(IOException("Offline"))
            override fun getPlaybackCapability(): PlaybackCapability = PlaybackCapability(ComplianceMode.OFFLINE_SYNTHETIC)
        }

        val offlineRepo = MusicRepository(
            provider = failingProvider,
            favoriteDao = database.favoriteDao(),
            recentPlaybackDao = database.recentPlaybackDao(),
            playlistDao = database.playlistDao(),
            searchHistoryDao = database.searchHistoryDao(),
            uploadedTrackDao = database.uploadedTrackDao(),
            discoveryCacheDao = database.discoveryCacheDao()
        )

        // 3. User browses discover feed offline: repository falls back to Room cache seamlessly
        val offlineFeedResult = offlineRepo.getDiscoverFeed().first()
        assertTrue(offlineFeedResult.isSuccess)
        val offlineFeed = offlineFeedResult.getOrThrow()
        assertTrue("Offline feed should contain cached trending tracks", offlineFeed.trendingTracks.isNotEmpty())

        // 4. User browses discovery history tab offline
        val offlineHistory = offlineRepo.getDiscoveryHistory().first()
        assertEquals(cachedTracks.size, offlineHistory.size)

        // 5. User searches offline: repository queries Room cache for matching metadata
        val firstTrackTitle = cachedTracks.first().title
        val offlineSearch = offlineRepo.searchWithScope(firstTrackTitle.take(4), SearchScope.ALL).first()
        assertTrue(offlineSearch.isSuccess)
        assertTrue(offlineSearch.getOrThrow().tracks.any { it.id == cachedTracks.first().id })

        // 6. User clears discovery history
        repository.clearDiscoveryHistory()
        assertTrue(repository.getDiscoveryHistory().first().isEmpty())
    }

    @Test
    fun `youtube provider resolves direct video id and searches curated fallback`() = runBlocking {
        val ytProvider = com.example.data.provider.YouTubeProvider()

        // 1. Direct YouTube video ID resolution
        val directResult = ytProvider.searchWithScope("DWcJFNfaw9c", "YOUTUBE")
        assertTrue(directResult.isSuccess)
        val directTracks = directResult.getOrThrow().tracks
        assertEquals(1, directTracks.size)
        assertEquals("DWcJFNfaw9c", directTracks.first().youtubeVideoId)
        assertTrue(directTracks.first().id.startsWith("yt_DWcJFNfaw9c"))

        // 2. Direct YouTube full URL resolution
        val urlResult = ytProvider.searchWithScope("https://www.youtube.com/watch?v=DWcJFNfaw9c", "ALL")
        assertTrue(urlResult.isSuccess)
        val urlTracks = urlResult.getOrThrow().tracks
        assertEquals(1, urlTracks.size)
        assertEquals("DWcJFNfaw9c", urlTracks.first().youtubeVideoId)

        // 3. Search query matching curated tracks in provider
        val curatedResult = ytProvider.searchWithScope("Solar Flares", "ALL")
        assertTrue(curatedResult.isSuccess)
        val results = curatedResult.getOrThrow()
        assertTrue(results.tracks.any { it.title.contains("Solar Flares", ignoreCase = true) })
    }

    @Test
    fun `verify developer profile attributes in resources`() {
        assertEquals("VHUWON MATHERS", context.getString(R.string.developer_name))
        assertEquals("vhuwonmathers@gmail.com", context.getString(R.string.developer_email))
        assertEquals("sudovu", context.getString(R.string.developer_username))
        assertEquals("9869367788", context.getString(R.string.developer_phone))
    }

    @Test
    fun `verify app name resource is GAMA`() {
        assertEquals("GAMA", context.getString(R.string.app_name))
    }

    @Test
    fun `verify equalizer settings presets and custom tuning`() {
        // Initially Flat
        assertEquals(com.example.domain.model.TuningPreset.FLAT, playbackManager.playbackState.value.equalizerSettings.currentPreset)

        // Switch to Bass Boost
        playbackManager.setTuningPreset(com.example.domain.model.TuningPreset.BASS_BOOST)
        val bassBoostSettings = playbackManager.playbackState.value.equalizerSettings
        assertEquals(com.example.domain.model.TuningPreset.BASS_BOOST, bassBoostSettings.currentPreset)
        assertTrue(bassBoostSettings.bands[0].gainDb > 5.0f) // 60Hz boosted

        // Manual slider change on 14kHz should transition to CUSTOM
        playbackManager.setEqualizerBand(4, -6.5f)
        val customSettings = playbackManager.playbackState.value.equalizerSettings
        assertEquals(com.example.domain.model.TuningPreset.CUSTOM, customSettings.currentPreset)
        assertEquals(-6.5f, customSettings.bands[4].gainDb, 0.01f)

        // Master gain adjustment
        playbackManager.setMasterGain(1.25f)
        assertEquals(1.25f, playbackManager.playbackState.value.equalizerSettings.masterGain, 0.01f)
    }

    @Test
    fun `verify smart queue recommendation logic`() {
        val track1 = Track(
            id = "t1",
            title = "Ambient Alpha",
            artist = "A",
            artistId = "a1",
            albumTitle = "Alb",
            albumId = "alb1",
            durationSeconds = 180,
            artworkUrl = "",
            genre = "Cosmic Ambient",
            frequencyHz = 432
        )
        val candidateSame = Track(
            id = "t2",
            title = "Ambient Beta",
            artist = "B",
            artistId = "a2",
            albumTitle = "Alb",
            albumId = "alb1",
            durationSeconds = 200,
            artworkUrl = "",
            genre = "Cosmic Ambient",
            frequencyHz = 432
        )
        val candidateDifferent = Track(
            id = "t3",
            title = "Hard Rock Gamma",
            artist = "C",
            artistId = "a3",
            albumTitle = "Alb2",
            albumId = "alb2",
            durationSeconds = 210,
            artworkUrl = "",
            genre = "Heavy Metal",
            frequencyHz = 110
        )

        val profile = com.example.core.media.SmartQueueEngine.analyzePlayedTracks(listOf(track1))
        assertEquals("Cosmic Ambient", profile.dominantGenre)

        val scoreSame = com.example.core.media.SmartQueueEngine.scoreCandidate(candidateSame, profile, listOf(track1), setOf(track1.id))
        val scoreDiff = com.example.core.media.SmartQueueEngine.scoreCandidate(candidateDifferent, profile, listOf(track1), setOf(track1.id))
        assertTrue("Matching genre must score significantly higher", scoreSame > scoreDiff)
    }

    @Test
    fun `verify permanent developer profile image resource exists`() {
        val drawable = context.resources.getDrawable(R.drawable.img_vhuwon_profile, null)
        assertNotNull("Permanent developer profile image must exist in drawables", drawable)
    }

    @Test
    fun `verify music downloader and local file playback resolution`() = runBlocking {
        val downloader = playbackManager.musicDownloader
        val track = Track(
            id = "test_download_trk_1",
            title = "Orbital Soundwave",
            artist = "Vektor Orbit",
            durationSeconds = 180,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        )

        // Initially not downloaded
        assertFalse(downloader.isTrackDownloaded(track.id))
        assertFalse(playbackManager.isTrackDownloaded(track.id))

        // Simulate local downloaded file creation in GAMA storage
        val targetFile = downloader.getTrackFile(track.id)
        targetFile.parentFile?.mkdirs()
        targetFile.writeBytes(ByteArray(2048) { 1 })

        // Re-scan
        downloader.scanExistingDownloads()
        assertTrue(downloader.isTrackDownloaded(track.id))
        assertTrue(playbackManager.isTrackDownloaded(track.id))
        assertEquals(targetFile.absolutePath, playbackManager.getDownloadedFile(track.id)?.absolutePath)

        // When track is played, localAudioUri is automatically resolved to local file
        playbackManager.playTrack(track)
        val current = playbackManager.playbackState.value.currentTrack
        assertNotNull(current)
        assertEquals(targetFile.absolutePath, current?.localAudioUri)
        assertTrue(current?.isDownloaded == true)

        // Cleanup
        downloader.deleteDownload(track.id)
        assertFalse(downloader.isTrackDownloaded(track.id))
    }

    @Test
    fun `verify library view model contains downloaded tab and reactive states`() = runBlocking {
        val libraryViewModel = com.example.feature.library.LibraryViewModel(repository, playbackManager)

        // Verify DOWNLOADED tab exists in entries
        assertTrue(com.example.feature.library.LibraryTab.entries.contains(com.example.feature.library.LibraryTab.DOWNLOADED))

        // Select DOWNLOADED tab
        libraryViewModel.selectTab(com.example.feature.library.LibraryTab.DOWNLOADED)
        assertEquals(com.example.feature.library.LibraryTab.DOWNLOADED, libraryViewModel.selectedTab.value)
    }

    @Test
    fun `playback manager time update ignores zero reset glitch`() {
        val track = Track(id = "trk_time_test", title = "Time Test", artist = "Artist", durationSeconds = 300)
        playbackManager.playTrack(track)

        // Valid update
        playbackManager.onBridgeTimeUpdate(15.5f, 300f)
        assertEquals(15500L, playbackManager.playbackState.value.positionMs)

        // Zero reset glitch from unstarted/hidden webview must be ignored
        playbackManager.onBridgeTimeUpdate(0f, 0f)
        assertEquals("Position must not reset to 0L on 0s glitch", 15500L, playbackManager.playbackState.value.positionMs)
    }
}
