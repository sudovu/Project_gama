package com.example.core.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.URL

class GamaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "gama_playback_channel"
        const val NOTIFICATION_ID = 4040

        const val ACTION_START = "com.aistudio.gamma.action.START"
        const val ACTION_PLAY = "com.aistudio.gamma.action.PLAY"
        const val ACTION_PAUSE = "com.aistudio.gamma.action.PAUSE"
        const val ACTION_TOGGLE = "com.aistudio.gamma.action.TOGGLE"
        const val ACTION_NEXT = "com.aistudio.gamma.action.NEXT"
        const val ACTION_PREVIOUS = "com.aistudio.gamma.action.PREVIOUS"
        const val ACTION_STOP = "com.aistudio.gamma.action.STOP"
        const val ACTION_UPDATE = "com.aistudio.gamma.action.UPDATE"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_ART_URL = "extra_art_url"

        fun start(context: Context, title: String, artist: String, isPlaying: Boolean, artUrl: String = "") {
            val intent = Intent(context, GamaPlaybackService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_ART_URL, artUrl)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("GamaPlaybackService", "Failed to start foreground service", e)
            }
        }

        fun update(context: Context, title: String, artist: String, isPlaying: Boolean, artUrl: String = "") {
            val intent = Intent(context, GamaPlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_ART_URL, artUrl)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("GamaPlaybackService", "Failed to update foreground service", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, GamaPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var currentTitle: String = "GAMA Track"
    private var currentArtist: String = "GAMA Music"
    private var isPlaying: Boolean = false
    private var currentArtUrl: String = ""
    private var currentBitmap: Bitmap? = null
    private var mediaSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaSession = MediaSessionCompat(this, "GamaPlaybackService").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    PlaybackManager.activeInstance?.play()
                }
                override fun onPause() {
                    PlaybackManager.activeInstance?.pause()
                }
                override fun onSkipToNext() {
                    PlaybackManager.activeInstance?.skipNext()
                }
                override fun onSkipToPrevious() {
                    PlaybackManager.activeInstance?.skipPrevious()
                }
                override fun onStop() {
                    PlaybackManager.activeInstance?.pause()
                    stopSelf()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            PlaybackManager.activeInstance?.pause()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.cancel(NOTIFICATION_ID)
            stopSelf()
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_START, ACTION_UPDATE -> {
                currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: currentTitle
                currentArtist = intent.getStringExtra(EXTRA_ARTIST) ?: currentArtist
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying)
                val newArtUrl = intent.getStringExtra(EXTRA_ART_URL) ?: ""

                if (newArtUrl.isNotEmpty() && newArtUrl != currentArtUrl) {
                    currentArtUrl = newArtUrl
                    fetchArtwork(newArtUrl)
                }

                syncMediaSession()
                promoteToForeground(buildNotification())
            }
            ACTION_PLAY -> {
                isPlaying = true
                PlaybackManager.activeInstance?.play()
                syncMediaSession()
                promoteToForeground(buildNotification())
            }
            ACTION_PAUSE -> {
                isPlaying = false
                PlaybackManager.activeInstance?.pause()
                syncMediaSession()
                promoteToForeground(buildNotification())
            }
            ACTION_TOGGLE -> {
                isPlaying = !isPlaying
                PlaybackManager.activeInstance?.togglePlayPause()
                promoteToForeground(buildNotification())
            }
            ACTION_NEXT -> {
                PlaybackManager.activeInstance?.skipNext()
            }
            ACTION_PREVIOUS -> {
                PlaybackManager.activeInstance?.skipPrevious()
            }
            ACTION_STOP -> {
                PlaybackManager.activeInstance?.pause()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun promoteToForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("GamaPlaybackService", "Error entering startForeground", e)
        }
    }

    private fun fetchArtwork(urlStr: String) {
        serviceScope.launch {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection()
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                val input = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                if (bitmap != null) {
                    currentBitmap = bitmap
                    syncMediaSession()
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, buildNotification())
                }
            } catch (_: Exception) {}
        }
    }

    private fun syncMediaSession() {
        try {
            val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            mediaSession?.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                    .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                    .build()
            )

            val metaBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "GAMA")
            if (currentBitmap != null) {
                metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentBitmap)
            }
            mediaSession?.setMetadata(metaBuilder.build())
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GAMA Playback Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background playback and lock screen controls for GAMA"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, GamaPlaybackService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, GamaPlaybackService::class.java).apply { action = playPauseAction },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, GamaPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, GamaPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSubText("GAMA")
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .setStyle(mediaStyle)

        if (currentBitmap != null) {
            builder.setLargeIcon(currentBitmap)
        }

        return builder.build()
    }
}
