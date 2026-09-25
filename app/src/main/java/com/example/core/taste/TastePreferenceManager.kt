package com.example.core.taste

import android.content.Context
import android.content.SharedPreferences
import com.example.core.media.SmartQueueEngine
import com.example.data.provider.YouTubeProvider
import com.example.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TastePreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gamma_taste_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isSpotifyLinked = MutableStateFlow(prefs.getBoolean(KEY_SPOTIFY_LINKED, false))
    val isSpotifyLinked: StateFlow<Boolean> = _isSpotifyLinked.asStateFlow()

    private val _spotifyUsername = MutableStateFlow(prefs.getString(KEY_SPOTIFY_USER, "") ?: "")
    val spotifyUsername: StateFlow<String> = _spotifyUsername.asStateFlow()

    private val _spotifyTastes = MutableStateFlow(loadList(KEY_SPOTIFY_TASTES, listOf("Rock & Metal", "Alternative Rock", "Cyberpunk", "Nu Metal")))
    val spotifyTastes: StateFlow<List<String>> = _spotifyTastes.asStateFlow()

    private val _spotifyTracks = MutableStateFlow<List<Track>>(emptyList())
    val spotifyTracks: StateFlow<List<Track>> = _spotifyTracks.asStateFlow()

    private val _spotifyEmailStatus = MutableStateFlow(
        try {
            EmailValidationStatus.valueOf(prefs.getString(KEY_SPOTIFY_STATUS, EmailValidationStatus.UNVERIFIED.name) ?: EmailValidationStatus.UNVERIFIED.name)
        } catch (_: Exception) {
            EmailValidationStatus.UNVERIFIED
        }
    )
    val spotifyEmailStatus: StateFlow<EmailValidationStatus> = _spotifyEmailStatus.asStateFlow()

    private val _spotifyEmailMessage = MutableStateFlow(prefs.getString(KEY_SPOTIFY_MSG, "") ?: "")
    val spotifyEmailMessage: StateFlow<String> = _spotifyEmailMessage.asStateFlow()

    private val _isGoogleLinked = MutableStateFlow(prefs.getBoolean(KEY_GOOGLE_LINKED, false))
    val isGoogleLinked: StateFlow<Boolean> = _isGoogleLinked.asStateFlow()

    private val _googleEmail = MutableStateFlow(
        prefs.getString(KEY_GOOGLE_EMAIL, "")?.let { stored ->
            if (stored.trim() == "@gmail.com" || stored.trim().startsWith("@") && stored.trim().length <= 11) {
                "vhuwonmathers@gmail.com".also { sanitized ->
                    prefs.edit().putString(KEY_GOOGLE_EMAIL, sanitized).apply()
                }
            } else {
                stored
            }
        } ?: ""
    )
    val googleEmail: StateFlow<String> = _googleEmail.asStateFlow()

    private val _youtubeTastes = MutableStateFlow(loadList(KEY_YOUTUBE_TASTES, listOf("432Hz Ambient", "Progressive Metal", "Synthwave", "Heavy Drums")))
    val youtubeTastes: StateFlow<List<String>> = _youtubeTastes.asStateFlow()

    private val _googleTracks = MutableStateFlow<List<Track>>(emptyList())
    val googleTracks: StateFlow<List<Track>> = _googleTracks.asStateFlow()

    private val _googleEmailStatus = MutableStateFlow(
        try {
            EmailValidationStatus.valueOf(prefs.getString(KEY_GOOGLE_STATUS, EmailValidationStatus.UNVERIFIED.name) ?: EmailValidationStatus.UNVERIFIED.name)
        } catch (_: Exception) {
            EmailValidationStatus.UNVERIFIED
        }
    )
    val googleEmailStatus: StateFlow<EmailValidationStatus> = _googleEmailStatus.asStateFlow()

    private val _googleEmailMessage = MutableStateFlow(prefs.getString(KEY_GOOGLE_MSG, "") ?: "")
    val googleEmailMessage: StateFlow<String> = _googleEmailMessage.asStateFlow()

    private val _isSpotifySyncing = MutableStateFlow(false)
    val isSpotifySyncing: StateFlow<Boolean> = _isSpotifySyncing.asStateFlow()

    private val _isGoogleSyncing = MutableStateFlow(false)
    val isGoogleSyncing: StateFlow<Boolean> = _isGoogleSyncing.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    init {
        // Background verification check on startup if accounts are linked
        if (_isGoogleLinked.value && _googleEmail.value.isNotBlank()) {
            if (_googleEmailStatus.value == EmailValidationStatus.UNVERIFIED || _googleEmailStatus.value == EmailValidationStatus.VALIDATING) {
                validateGoogleEmailBackground(_googleEmail.value)
            }
        }
        if (_isSpotifyLinked.value && _spotifyUsername.value.isNotBlank()) {
            if (_spotifyEmailStatus.value == EmailValidationStatus.UNVERIFIED || _spotifyEmailStatus.value == EmailValidationStatus.VALIDATING) {
                validateSpotifyEmailBackground(_spotifyUsername.value)
            }
        }
    }

    fun validateGoogleEmailBackground(email: String = _googleEmail.value) {
        val target = email.trim().ifEmpty { _googleEmail.value }
        if (target.isBlank()) {
            _googleEmailStatus.value = EmailValidationStatus.INVALID
            _googleEmailMessage.value = "Email address cannot be empty."
            prefs.edit().putString(KEY_GOOGLE_STATUS, EmailValidationStatus.INVALID.name)
                .putString(KEY_GOOGLE_MSG, "Email address cannot be empty.").apply()
            return
        }

        _googleEmailStatus.value = EmailValidationStatus.VALIDATING
        _googleEmailMessage.value = "Validating in background..."
        prefs.edit().putString(KEY_GOOGLE_STATUS, EmailValidationStatus.VALIDATING.name).apply()

        scope.launch {
            val result = EmailValidator.validateEmailInBackground(target)
            _googleEmailStatus.value = result.status
            _googleEmailMessage.value = result.message
            prefs.edit()
                .putString(KEY_GOOGLE_STATUS, result.status.name)
                .putString(KEY_GOOGLE_MSG, result.message)
                .apply()
        }
    }

    fun validateSpotifyEmailBackground(usernameOrEmail: String = _spotifyUsername.value) {
        val target = usernameOrEmail.trim().ifEmpty { _spotifyUsername.value }
        if (target.isBlank()) {
            _spotifyEmailStatus.value = EmailValidationStatus.INVALID
            _spotifyEmailMessage.value = "Account identifier cannot be empty."
            prefs.edit().putString(KEY_SPOTIFY_STATUS, EmailValidationStatus.INVALID.name)
                .putString(KEY_SPOTIFY_MSG, "Account identifier cannot be empty.").apply()
            return
        }

        _spotifyEmailStatus.value = EmailValidationStatus.VALIDATING
        _spotifyEmailMessage.value = "Validating in background..."
        prefs.edit().putString(KEY_SPOTIFY_STATUS, EmailValidationStatus.VALIDATING.name).apply()

        scope.launch {
            if (target.contains("@")) {
                val result = EmailValidator.validateEmailInBackground(target)
                _spotifyEmailStatus.value = result.status
                _spotifyEmailMessage.value = result.message
                prefs.edit()
                    .putString(KEY_SPOTIFY_STATUS, result.status.name)
                    .putString(KEY_SPOTIFY_MSG, result.message)
                    .apply()
            } else {
                // Spotify username format
                val isValid = target.length in 3..30 && !target.contains(" ")
                val status = if (isValid) EmailValidationStatus.VERIFIED else EmailValidationStatus.INVALID
                val msg = if (isValid) "Username verified" else "Username must be 3-30 characters with no spaces"
                _spotifyEmailStatus.value = status
                _spotifyEmailMessage.value = msg
                prefs.edit()
                    .putString(KEY_SPOTIFY_STATUS, status.name)
                    .putString(KEY_SPOTIFY_MSG, msg)
                    .apply()
            }
        }
    }

    fun linkSpotify(username: String = "vhuwonmathers@gmail.com", tastes: List<String> = listOf("Rock & Metal", "Alternative Rock", "Cyberpunk")) {
        val cleanUser = username.trim().ifEmpty { "vhuwonmathers@gmail.com" }
        prefs.edit()
            .putBoolean(KEY_SPOTIFY_LINKED, true)
            .putString(KEY_SPOTIFY_USER, cleanUser)
            .putString(KEY_SPOTIFY_TASTES, tastes.joinToString(","))
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()

        _isSpotifyLinked.value = true
        _spotifyUsername.value = cleanUser
        _spotifyTastes.value = tastes
        _lastSyncTime.value = System.currentTimeMillis()

        validateSpotifyEmailBackground(cleanUser)
    }

    fun unlinkSpotify() {
        prefs.edit()
            .putBoolean(KEY_SPOTIFY_LINKED, false)
            .remove(KEY_SPOTIFY_USER)
            .remove(KEY_SPOTIFY_STATUS)
            .remove(KEY_SPOTIFY_MSG)
            .apply()

        _isSpotifyLinked.value = false
        _spotifyUsername.value = ""
        _spotifyTracks.value = emptyList()
        _spotifyEmailStatus.value = EmailValidationStatus.UNVERIFIED
        _spotifyEmailMessage.value = ""
    }

    fun linkGoogle(email: String = "vhuwonmathers@gmail.com", tastes: List<String> = listOf("432Hz Ambient", "Progressive Metal", "Synthwave")) {
        val cleanEmail = email.trim().let {
            if (it.isEmpty() || it == "@gmail.com" || it.startsWith("@")) "vhuwonmathers@gmail.com" else it
        }
        prefs.edit()
            .putBoolean(KEY_GOOGLE_LINKED, true)
            .putString(KEY_GOOGLE_EMAIL, cleanEmail)
            .putString(KEY_YOUTUBE_TASTES, tastes.joinToString(","))
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()

        _isGoogleLinked.value = true
        _googleEmail.value = cleanEmail
        _youtubeTastes.value = tastes
        _lastSyncTime.value = System.currentTimeMillis()

        validateGoogleEmailBackground(cleanEmail)
    }

    fun unlinkGoogle() {
        prefs.edit()
            .putBoolean(KEY_GOOGLE_LINKED, false)
            .remove(KEY_GOOGLE_EMAIL)
            .remove(KEY_GOOGLE_STATUS)
            .remove(KEY_GOOGLE_MSG)
            .apply()

        _isGoogleLinked.value = false
        _googleEmail.value = ""
        _googleTracks.value = emptyList()
        _googleEmailStatus.value = EmailValidationStatus.UNVERIFIED
        _googleEmailMessage.value = ""
    }

    suspend fun syncSpotifyLiveTastes(youtubeProvider: YouTubeProvider) = withContext(Dispatchers.IO) {
        _isSpotifySyncing.value = true
        _isSyncing.value = true
        try {
            if (_isSpotifyLinked.value) {
                // Verify email / handle before syncing
                val user = _spotifyUsername.value
                if (user.contains("@")) {
                    val valResult = EmailValidator.validateEmailInBackground(user)
                    _spotifyEmailStatus.value = valResult.status
                    _spotifyEmailMessage.value = valResult.message
                    prefs.edit()
                        .putString(KEY_SPOTIFY_STATUS, valResult.status.name)
                        .putString(KEY_SPOTIFY_MSG, valResult.message)
                        .apply()

                    if (!valResult.isValid) {
                        android.util.Log.w("TastePreference", "Spotify email verification failed: ${valResult.message}. Skipping sync.")
                        return@withContext
                    }
                }

                val querySuffixes = listOf(
                    "hits single official music video",
                    "top songs official video",
                    "popular tracks official single",
                    "best music hits official video"
                )
                val querySuffix = querySuffixes.random()
                val tastes = _spotifyTastes.value.take(2)
                val deferred = tastes.map { taste ->
                    async {
                        try {
                            val res = youtubeProvider.searchWithScope("$taste $querySuffix", "YOUTUBE_MUSIC")
                            res.getOrNull()?.tracks
                                ?.filter { !SmartQueueEngine.isCollectionOrMix(it.title, it.durationSeconds) }
                                ?.take(6)
                                ?.map { it.copy(genre = taste) }
                                ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }
                val allFetched = deferred.flatMap { it.await() }
                if (allFetched.isNotEmpty()) {
                    _spotifyTracks.value = SmartQueueEngine.deduplicateTracks(allFetched)
                }
            }
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
            _lastSyncTime.value = now
        } catch (e: Exception) {
            android.util.Log.e("TastePreference", "Error syncing Spotify tastes", e)
        } finally {
            _isSpotifySyncing.value = false
            _isSyncing.value = _isGoogleSyncing.value
        }
    }

    suspend fun syncYouTubeLiveTastes(youtubeProvider: YouTubeProvider) = withContext(Dispatchers.IO) {
        _isGoogleSyncing.value = true
        _isSyncing.value = true
        try {
            if (_isGoogleLinked.value) {
                // Verify email before syncing
                val email = _googleEmail.value
                val valResult = EmailValidator.validateEmailInBackground(email)
                _googleEmailStatus.value = valResult.status
                _googleEmailMessage.value = valResult.message
                prefs.edit()
                    .putString(KEY_GOOGLE_STATUS, valResult.status.name)
                    .putString(KEY_GOOGLE_MSG, valResult.message)
                    .apply()

                if (!valResult.isValid) {
                    android.util.Log.w("TastePreference", "Google/YouTube email verification failed: ${valResult.message}. Skipping sync.")
                    return@withContext
                }

                val querySuffixes = listOf(
                    "hits single official music video",
                    "top songs official video",
                    "popular tracks official single",
                    "best music hits official video"
                )
                val querySuffix = querySuffixes.random()
                val tastes = _youtubeTastes.value.take(2)
                val deferred = tastes.map { taste ->
                    async {
                        try {
                            val res = youtubeProvider.searchWithScope("$taste $querySuffix", "YOUTUBE_MUSIC")
                            res.getOrNull()?.tracks
                                ?.filter { !SmartQueueEngine.isCollectionOrMix(it.title, it.durationSeconds) }
                                ?.take(6)
                                ?.map { it.copy(genre = taste) }
                                ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }
                val allFetched = deferred.flatMap { it.await() }
                if (allFetched.isNotEmpty()) {
                    _googleTracks.value = SmartQueueEngine.deduplicateTracks(allFetched)
                }
            }
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
            _lastSyncTime.value = now
        } catch (e: Exception) {
            android.util.Log.e("TastePreference", "Error syncing YouTube tastes", e)
        } finally {
            _isGoogleSyncing.value = false
            _isSyncing.value = _isSpotifySyncing.value
        }
    }

    suspend fun syncLiveTastes(youtubeProvider: YouTubeProvider) = coroutineScope {
        val d1 = async { syncSpotifyLiveTastes(youtubeProvider) }
        val d2 = async { syncYouTubeLiveTastes(youtubeProvider) }
        d1.await()
        d2.await()
    }

    fun syncSpotifyTastes() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
        _lastSyncTime.value = now
    }

    fun syncGoogleTastes() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
        _lastSyncTime.value = now
    }

    fun syncAllTastes() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
        _lastSyncTime.value = now
    }

    fun getSpotifyAdaptedRecommendations(catalog: List<Track>): List<Track> {
        if (!_isSpotifyLinked.value) return emptyList()
        val liveTracks = _spotifyTracks.value
        if (liveTracks.isNotEmpty()) return liveTracks
        val activeTastes = _spotifyTastes.value
        return catalog.filter { track ->
            activeTastes.any { taste ->
                track.genre.contains(taste, ignoreCase = true) ||
                track.title.contains(taste, ignoreCase = true) ||
                track.artist.contains(taste, ignoreCase = true)
            }
        }.ifEmpty { catalog.take(6) }
    }

    fun getYouTubeAdaptedRecommendations(catalog: List<Track>): List<Track> {
        if (!_isGoogleLinked.value) return emptyList()
        val liveTracks = _googleTracks.value
        if (liveTracks.isNotEmpty()) return liveTracks
        val activeTastes = _youtubeTastes.value
        return catalog.filter { track ->
            activeTastes.any { taste ->
                track.genre.contains(taste, ignoreCase = true) ||
                (taste.contains("432", ignoreCase = true) && track.frequencyHz == 432) ||
                track.title.contains(taste, ignoreCase = true)
            }
        }.ifEmpty { catalog.reversed().take(6) }
    }

    private fun loadList(key: String, defaultList: List<String>): List<String> {
        val saved = prefs.getString(key, null) ?: return defaultList
        return if (saved.isBlank()) defaultList else saved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        private const val KEY_SPOTIFY_LINKED = "spotify_linked"
        private const val KEY_SPOTIFY_USER = "spotify_username"
        private const val KEY_SPOTIFY_TASTES = "spotify_tastes"
        private const val KEY_SPOTIFY_STATUS = "spotify_email_status"
        private const val KEY_SPOTIFY_MSG = "spotify_email_msg"
        private const val KEY_GOOGLE_LINKED = "google_linked"
        private const val KEY_GOOGLE_EMAIL = "google_email"
        private const val KEY_GOOGLE_STATUS = "google_email_status"
        private const val KEY_GOOGLE_MSG = "google_email_msg"
        private const val KEY_YOUTUBE_TASTES = "youtube_tastes"
        private const val KEY_LAST_SYNC = "last_sync_time"

        @Volatile
        private var INSTANCE: TastePreferenceManager? = null

        fun getInstance(context: Context): TastePreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TastePreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
