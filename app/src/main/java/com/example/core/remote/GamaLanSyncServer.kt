package com.example.core.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.Log
import com.example.core.media.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object GamaLanSyncServer {

    private const val TAG = "GamaLanSyncServer"
    const val DEFAULT_PORT = 8765
    const val SERVICE_TYPE = "_gama-sync._tcp"

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private var broadcastJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectedClients = CopyOnWriteArrayList<Socket>()
    private var playbackManager: PlaybackManager? = null
    private var appContext: Context? = null

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null

    @Volatile
    var isRunning = false
        private set

    @Volatile
    var serverIp = "127.0.0.1"
        private set

    @Volatile
    var pairingPin: String = generateNewPin()
        private set

    private val sessionTokens = ConcurrentHashMap.newKeySet<String>()

    fun resetPin(): String {
        pairingPin = generateNewPin()
        sessionTokens.clear()
        Log.i(TAG, "GAMA LAN Sync PIN reset to $pairingPin; all prior sessions revoked.")
        return pairingPin
    }

    private fun generateNewPin(): String = String.format("%04d", (1000..9999).random())

    fun pairWithPin(pin: String): String? {
        if (pin.trim() == pairingPin) {
            val token = UUID.randomUUID().toString().replace("-", "")
            sessionTokens.add(token)
            Log.i(TAG, "Client successfully paired with PIN. Generated session token: ${token.take(6)}...")
            return token
        }
        return null
    }

    fun validateToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return sessionTokens.contains(token.trim())
    }

    fun addTestSessionToken(token: String) {
        sessionTokens.add(token.trim())
    }

    fun clearSessionTokens() {
        sessionTokens.clear()
    }

    fun start(context: Context, manager: PlaybackManager, port: Int = DEFAULT_PORT) {
        if (isRunning) return
        appContext = context.applicationContext
        playbackManager = manager
        serverIp = resolveLocalIp(context)

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.i(TAG, "GAMA LAN Sync Server active at ws://$serverIp:$port/ws (HTTP: http://$serverIp:$port)")

                // Register mDNS Zero-Config Discovery
                registerMdnsService(context, port)

                startBroadcastLoop()

                while (isActive) {
                    val client = serverSocket?.accept() ?: break
                    scope.launch {
                        handleClient(client)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "Server socket exception: ${e.message}")
                }
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        if (!isRunning && serverSocket == null) return
        isRunning = false
        appContext = null
        unregisterMdnsService()
        serverJob?.cancel()
        broadcastJob?.cancel()
        connectedClients.forEach {
            try { it.close() } catch (_: Exception) {}
        }
        connectedClients.clear()
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        Log.i(TAG, "GamaLanSyncServer stopped cleanly. Sockets closed, mDNS unregistered.")
    }

    private fun registerMdnsService(context: Context, port: Int) {
        try {
            nsdManager = context.applicationContext.getSystemService(Context.NSD_SERVICE) as? NsdManager
            val cleanModel = Build.MODEL.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "GAMA-$cleanModel"
                serviceType = SERVICE_TYPE
                setPort(port)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAttribute("model", Build.MODEL)
                    setAttribute("version", "1.2.0")
                    setAttribute("port", port.toString())
                }
            }

            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(service: NsdServiceInfo) {
                    Log.i(TAG, "mDNS Service registered: ${service.serviceName} (${service.serviceType}) on port $port")
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "mDNS Service registration failed with code: $errorCode")
                }
                override fun onServiceUnregistered(service: NsdServiceInfo) {
                    Log.i(TAG, "mDNS Service unregistered: ${service.serviceName}")
                }
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "mDNS Service unregistration failed with code: $errorCode")
                }
            }

            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register mDNS service: ${e.message}")
        }
    }

    private fun unregisterMdnsService() {
        try {
            registrationListener?.let {
                nsdManager?.unregisterService(it)
            }
        } catch (_: Exception) {}
        registrationListener = null
        nsdManager = null
    }

    private fun startBroadcastLoop() {
        broadcastJob?.cancel()
        broadcastJob = scope.launch {
            while (isActive && isRunning) {
                delay(100) // ~10Hz smooth telemetry updates for FFT and position
                if (connectedClients.isNotEmpty()) {
                    val statusJson = buildStatusJson().toString()
                    val frame = createWebSocketTextFrame(statusJson)
                    val dead = mutableListOf<Socket>()
                    for (client in connectedClients) {
                        try {
                            client.getOutputStream().write(frame)
                            client.getOutputStream().flush()
                        } catch (_: Exception) {
                            dead.add(client)
                        }
                    }
                    connectedClients.removeAll(dead)
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = input.readLine() ?: return
            val headers = mutableMapOf<String, String>()
            var line: String?

            while (input.readLine().also { line = it } != null && line!!.isNotBlank()) {
                val parts = line!!.split(":", limit = 2)
                if (parts.size == 2) {
                    headers[parts[0].trim().lowercase()] = parts[1].trim()
                }
            }

            val parts = requestLine.split(" ")
            val method = parts.getOrNull(0) ?: "GET"
            val fullPath = parts.getOrNull(1) ?: "/"
            val path = fullPath.substringBefore("?")
            val queryString = if (fullPath.contains("?")) fullPath.substringAfter("?") else ""
            val queryParams = queryString.split("&").mapNotNull {
                val kv = it.split("=", limit = 2)
                if (kv.isNotEmpty()) kv[0] to (kv.getOrNull(1) ?: "") else null
            }.toMap()

            val isWebSocket = headers["upgrade"]?.equals("websocket", ignoreCase = true) == true

            if (isWebSocket) {
                // Check auth token for WebSocket: query param ?token=... or header Authorization: Bearer ...
                val token = queryParams["token"]
                    ?: headers["authorization"]?.removePrefix("Bearer ")?.trim()
                    ?: headers["x-auth-token"]

                if (!validateToken(token)) {
                    val unauthorized = "HTTP/1.1 401 Unauthorized\r\n" +
                            "Content-Type: application/json; charset=utf-8\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Content-Length: 59\r\n\r\n" +
                            "{\"success\":false,\"error\":\"Unauthorized. PIN pairing required.\"}"
                    output.write(unauthorized.toByteArray(Charsets.UTF_8))
                    output.flush()
                    socket.close()
                    return
                }

                // Perform RFC 6455 WebSocket Handshake
                val key = headers["sec-websocket-key"] ?: ""
                val acceptKey = computeWebSocketAcceptKey(key)

                val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: $acceptKey\r\n" +
                        "Access-Control-Allow-Origin: *\r\n\r\n"

                output.write(response.toByteArray(Charsets.UTF_8))
                output.flush()

                connectedClients.add(socket)
                listenWebSocketFrames(socket)
            } else {
                // Standard HTTP handling
                when {
                    method == "OPTIONS" -> {
                        val corsResponse = "HTTP/1.1 204 No Content\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                                "Access-Control-Allow-Headers: Content-Type, Authorization, X-Auth-Token\r\n\r\n"
                        output.write(corsResponse.toByteArray(Charsets.UTF_8))
                    }
                    method == "GET" && (path == "/" || path == "/index.html") && headers["accept"]?.contains("text/html") == true -> {
                        val html = generateWebCompanionHtml()
                        val bytes = html.toByteArray(Charsets.UTF_8)
                        val res = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html; charset=utf-8\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "Content-Length: ${bytes.size}\r\n\r\n" + html
                        output.write(res.toByteArray(Charsets.UTF_8))
                    }
                    method == "GET" && (path == "/" || path == "/status") -> {
                        val body = buildStatusJson().toString()
                        val res = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json; charset=utf-8\r\n" +
                                "Access-Control-Allow-Origin: *\r\n" +
                                "Content-Length: ${body.toByteArray().size}\r\n\r\n" + body
                        output.write(res.toByteArray(Charsets.UTF_8))
                    }
                    method == "POST" && path == "/pair" -> {
                        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                        val bodyChars = CharArray(contentLength)
                        input.read(bodyChars, 0, contentLength)
                        val bodyStr = String(bodyChars)
                        val pairJson = try { JSONObject(bodyStr) } catch (_: Exception) { JSONObject() }
                        val pin = pairJson.optString("pin", "").trim()
                        val token = pairWithPin(pin)

                        if (token != null) {
                            val resObj = JSONObject().apply {
                                put("success", true)
                                put("token", token)
                                put("deviceModel", Build.MODEL)
                                put("message", "Paired successfully")
                            }
                            val body = resObj.toString()
                            val res = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${body.toByteArray().size}\r\n\r\n" + body
                            output.write(res.toByteArray(Charsets.UTF_8))
                        } else {
                            val resObj = JSONObject().apply {
                                put("success", false)
                                put("error", "Invalid 4-digit pairing PIN")
                            }
                            val body = resObj.toString()
                            val res = "HTTP/1.1 401 Unauthorized\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${body.toByteArray().size}\r\n\r\n" + body
                            output.write(res.toByteArray(Charsets.UTF_8))
                        }
                    }
                    method == "POST" && path == "/command" -> {
                        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                        val bodyChars = CharArray(contentLength)
                        input.read(bodyChars, 0, contentLength)
                        val bodyStr = String(bodyChars)

                        val authHeader = headers["authorization"]?.removePrefix("Bearer ")?.trim()
                            ?: headers["x-auth-token"]
                        val jsonToken = try { JSONObject(bodyStr).optString("token", "") } catch (_: Exception) { "" }
                        val effectiveToken = if (!authHeader.isNullOrBlank()) authHeader else jsonToken

                        if (!validateToken(effectiveToken)) {
                            val errBody = "{\"success\":false,\"error\":\"Unauthorized. Valid pairing token required.\"}"
                            val res = "HTTP/1.1 401 Unauthorized\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${errBody.toByteArray().size}\r\n\r\n" + errBody
                            output.write(res.toByteArray(Charsets.UTF_8))
                        } else {
                            val result = executeCommand(bodyStr)
                            val res = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${result.toByteArray().size}\r\n\r\n" + result
                            output.write(res.toByteArray(Charsets.UTF_8))
                        }
                    }
                    method == "GET" && path == "/stream" -> {
                        val authHeader = headers["authorization"]?.removePrefix("Bearer ")?.trim()
                            ?: headers["x-auth-token"]
                        val effectiveToken = queryParams["token"] ?: authHeader

                        if (!validateToken(effectiveToken)) {
                            val errBody = "{\"success\":false,\"error\":\"Unauthorized. Valid pairing token required.\"}"
                            val res = "HTTP/1.1 401 Unauthorized\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${errBody.toByteArray().size}\r\n\r\n" + errBody
                            output.write(res.toByteArray(Charsets.UTF_8))
                        } else {
                            val track = playbackManager?.playbackState?.value?.currentTrack
                            handleStreamRequest(output, headers, track, appContext)
                        }
                    }
                    method == "POST" && path == "/queue/push" -> {
                        val authHeader = headers["authorization"]?.removePrefix("Bearer ")?.trim()
                            ?: headers["x-auth-token"]
                        val effectiveToken = queryParams["token"] ?: authHeader

                        if (!validateToken(effectiveToken)) {
                            val errBody = "{\"success\":false,\"error\":\"Unauthorized. Valid pairing token required.\"}"
                            val res = "HTTP/1.1 401 Unauthorized\r\n" +
                                    "Content-Type: application/json; charset=utf-8\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n" +
                                    "Content-Length: ${errBody.toByteArray().size}\r\n\r\n" + errBody
                            output.write(res.toByteArray(Charsets.UTF_8))
                        } else {
                            val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                            val contentType = headers["content-type"] ?: "application/json"

                            if (contentType.contains("application/json", ignoreCase = true)) {
                                val bodyChars = CharArray(contentLength)
                                input.read(bodyChars, 0, contentLength)
                                val bodyStr = String(bodyChars)
                                val json = try { JSONObject(bodyStr) } catch (_: Exception) { JSONObject() }
                                val playNow = json.optBoolean("playNow", false)

                                val pushedTrack = com.example.domain.model.Track(
                                    id = json.optString("id", "lan_" + UUID.randomUUID().toString().take(8)),
                                    title = json.optString("title", "LAN Stream Track"),
                                    artist = json.optString("artist", "Windows PC"),
                                    artworkUrl = json.optString("artworkUrl", ""),
                                    youtubeVideoId = json.optString("youtubeVideoId", ""),
                                    streamUrl = json.optString("streamUrl", ""),
                                    localAudioUri = json.optString("localAudioUri", ""),
                                    durationSeconds = json.optInt("durationSeconds", 0)
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    if (playNow) {
                                        playbackManager?.playTrack(pushedTrack)
                                    } else {
                                        playbackManager?.addToQueue(pushedTrack)
                                    }
                                }

                                val resBody = "{\"success\":true,\"message\":\"Track queued on phone\",\"trackId\":\"${pushedTrack.id}\"}"
                                val res = "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: application/json; charset=utf-8\r\n" +
                                        "Access-Control-Allow-Origin: *\r\n" +
                                        "Content-Length: ${resBody.toByteArray().size}\r\n\r\n" + resBody
                                output.write(res.toByteArray(Charsets.UTF_8))
                            } else {
                                val filename = headers["x-track-filename"] ?: "upload_${System.currentTimeMillis()}.mp3"
                                val title = headers["x-track-title"] ?: filename.substringBeforeLast(".")
                                val artist = headers["x-track-artist"] ?: "Windows PC"
                                val playNow = headers["x-play-now"]?.equals("true", ignoreCase = true) == true

                                val uploadDir = java.io.File(appContext?.cacheDir, "lan_queue").apply { mkdirs() }
                                cleanupLanQueueDirectory(uploadDir)
                                val cleanFilename = filename.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                                val savedFile = java.io.File(uploadDir, "${System.currentTimeMillis()}_$cleanFilename")

                                val fos = java.io.FileOutputStream(savedFile)
                                val buffer = CharArray(8192)
                                var bytesRemaining = contentLength
                                while (bytesRemaining > 0) {
                                    val toRead = bytesRemaining.coerceAtMost(buffer.size)
                                    val read = input.read(buffer, 0, toRead)
                                    if (read <= 0) break
                                    val bytes = String(buffer, 0, read).toByteArray(Charsets.ISO_8859_1)
                                    fos.write(bytes)
                                    bytesRemaining -= read
                                }
                                fos.flush()
                                fos.close()

                                val pushedTrack = com.example.domain.model.Track(
                                    id = "lan_" + UUID.randomUUID().toString().take(8),
                                    title = title,
                                    artist = artist,
                                    localAudioUri = savedFile.absolutePath,
                                    isDownloaded = true,
                                    codec = savedFile.extension.uppercase()
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    if (playNow) {
                                        playbackManager?.playTrack(pushedTrack)
                                    } else {
                                        playbackManager?.addToQueue(pushedTrack)
                                    }
                                }

                                val resBody = "{\"success\":true,\"message\":\"Audio file queued on phone\",\"trackId\":\"${pushedTrack.id}\"}"
                                val res = "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: application/json; charset=utf-8\r\n" +
                                        "Access-Control-Allow-Origin: *\r\n" +
                                        "Content-Length: ${resBody.toByteArray().size}\r\n\r\n" + resBody
                                output.write(res.toByteArray(Charsets.UTF_8))
                            }
                        }
                    }
                    else -> {
                        val notFound = "HTTP/1.1 404 Not Found\r\n\r\n"
                        output.write(notFound.toByteArray(Charsets.UTF_8))
                    }
                }
                output.flush()
                socket.close()
            }
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun listenWebSocketFrames(socket: Socket) {
        val inStream = socket.getInputStream()
        val buffer = ByteArray(4096)

        try {
            while (isRunning && !socket.isClosed) {
                val len = inStream.read(buffer)
                if (len <= 0) break

                val opcode = buffer[0].toInt() and 0x0F
                if (opcode == 8) { // Close frame
                    break
                }
                if (opcode == 1) { // Text frame
                    val isMasked = (buffer[1].toInt() and 0x80) != 0
                    var payloadLen = (buffer[1].toInt() and 0x7F).toLong()
                    var offset = 2

                    if (payloadLen == 126L) {
                        payloadLen = (((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)).toLong()
                        offset = 4
                    }

                    val maskKey = if (isMasked) {
                        val key = ByteArray(4)
                        System.arraycopy(buffer, offset, key, 0, 4)
                        offset += 4
                        key
                    } else null

                    val payload = ByteArray(payloadLen.toInt())
                    for (i in 0 until payloadLen.toInt()) {
                        val b = buffer[offset + i]
                        payload[i] = if (maskKey != null) (b.toInt() xor maskKey[i % 4].toInt()).toByte() else b
                    }

                    val text = String(payload, Charsets.UTF_8)
                    executeCommand(text)
                }
            }
        } catch (_: Exception) {
        } finally {
            connectedClients.remove(socket)
            try { socket.close() } catch (_: Exception) {}
        }
    }

    fun buildStatusJson(): JSONObject {
        val pm = playbackManager ?: return JSONObject().apply {
            put("error", "No manager active")
            put("pairingPin", pairingPin)
            put("deviceModel", Build.MODEL)
            put("protocolVersion", "1.2.0")
        }
        val state = pm.playbackState.value
        val track = state.currentTrack

        return JSONObject().apply {
            put("type", "STATUS")
            put("protocolVersion", "1.2.0")
            put("deviceModel", Build.MODEL)
            put("pairingPin", pairingPin)
            put("pinAuthRequired", true)
            put("activeSessions", sessionTokens.size)
            put("isPlaying", state.isPlaying)
            put("isBuffering", state.isBuffering)
            put("positionMs", state.positionMs)
            put("durationMs", state.durationMs)
            put("isShuffle", state.isShuffle)
            put("repeatMode", state.repeatMode.name)
            put("isAudioOnlyMode", state.isAudioOnlyMode)
            put("playbackSpeed", state.playbackSpeed)
            put("serverIp", serverIp)
            put("serverPort", DEFAULT_PORT)

            // Current Track Details
            if (track != null) {
                put("track", JSONObject().apply {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("artworkUrl", track.artworkUrl)
                    put("youtubeVideoId", track.youtubeVideoId)
                    put("durationSeconds", track.durationSeconds)
                    put("codec", track.codec)
                    put("audioQualityBadge", track.audioQualityBadge)
                })
            }

            // 16-band Spectrum Analyzer FFT Telemetry
            val bandsArray = JSONArray()
            state.visualizerBands.forEach { b -> bandsArray.put(b.toDouble()) }
            put("visualizerBands", bandsArray)

            // Queue summary
            val queueArray = JSONArray()
            state.queue.take(15).forEach { qTrack ->
                queueArray.put(JSONObject().apply {
                    put("id", qTrack.id)
                    put("title", qTrack.title)
                    put("artist", qTrack.artist)
                })
            }
            put("queue", queueArray)
        }
    }

    fun executeCommand(jsonString: String): String {
        val pm = playbackManager ?: return "{\"error\":\"playbackManager is null\"}"

        return try {
            val json = JSONObject(jsonString)
            val action = json.optString("action", "")

            CoroutineScope(Dispatchers.Main).launch {
                when (action) {
                    "play_pause", "toggle_play_pause" -> pm.togglePlayPause()
                    "play" -> if (!pm.playbackState.value.isPlaying) pm.togglePlayPause()
                    "pause" -> if (pm.playbackState.value.isPlaying) pm.togglePlayPause()
                    "next", "skip_next" -> pm.skipNext()
                    "prev", "previous", "skip_prev" -> pm.skipPrevious()
                    "seek" -> {
                        val pos = json.optLong("positionMs", 0L)
                        pm.seekTo(pos)
                    }
                    "toggle_shuffle" -> pm.toggleShuffle()
                    "cycle_repeat" -> pm.cycleRepeatMode()
                    "toggle_audio_only" -> pm.toggleAudioOnlyMode()
                    "set_speed" -> {
                        val speed = json.optDouble("speed", 1.0).toFloat()
                        pm.setPlaybackSpeed(speed)
                    }
                    "sleep_timer" -> {
                        val mins = json.optInt("minutes", 15)
                        pm.startSleepTimer(mins)
                    }
                    "cancel_sleep_timer" -> pm.cancelSleepTimer()
                }
            }

            JSONObject().apply {
                put("success", true)
                put("executedAction", action)
            }.toString()
        } catch (e: Exception) {
            "{\"error\":\"${e.message}\"}"
        }
    }

    private fun computeWebSocketAcceptKey(clientKey: String): String {
        val magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val md = MessageDigest.getInstance("SHA-1")
        val hash = md.digest((clientKey + magic).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun createWebSocketTextFrame(message: String): ByteArray {
        val payload = message.toByteArray(Charsets.UTF_8)
        val len = payload.size

        return when {
            len <= 125 -> {
                val frame = ByteArray(2 + len)
                frame[0] = 0x81.toByte() // FIN + Text
                frame[1] = len.toByte()
                System.arraycopy(payload, 0, frame, 2, len)
                frame
            }
            len <= 65535 -> {
                val frame = ByteArray(4 + len)
                frame[0] = 0x81.toByte()
                frame[1] = 126.toByte()
                frame[2] = ((len shr 8) and 0xFF).toByte()
                frame[3] = (len and 0xFF).toByte()
                System.arraycopy(payload, 0, frame, 4, len)
                frame
            }
            else -> {
                val frame = ByteArray(10 + len)
                frame[0] = 0x81.toByte()
                frame[1] = 127.toByte()
                for (i in 0..7) {
                    frame[2 + i] = ((len.toLong() shr ((7 - i) * 8)) and 0xFF).toByte()
                }
                System.arraycopy(payload, 0, frame, 10, len)
                frame
            }
        }
    }

    private fun handleStreamRequest(
        output: OutputStream,
        headers: Map<String, String>,
        track: com.example.domain.model.Track?,
        context: Context?
    ) {
        if (track == null) {
            val notFound = "HTTP/1.1 404 Not Found\r\nAccess-Control-Allow-Origin: *\r\n\r\n"
            output.write(notFound.toByteArray(Charsets.UTF_8))
            return
        }

        var targetFile: java.io.File? = null
        if (track.localAudioUri.isNotBlank()) {
            val path = track.localAudioUri.removePrefix("file://")
            val f = java.io.File(path)
            if (f.exists() && f.isFile) {
                targetFile = f
            }
        }

        if (targetFile == null && context != null) {
            val dlDir = java.io.File(context.filesDir, "downloads")
            val candidates = listOf(
                java.io.File(dlDir, "${track.id}.mp3"),
                java.io.File(dlDir, "${track.id}.flac"),
                java.io.File(dlDir, "${track.id}.m4a"),
                java.io.File(context.cacheDir, "lan_queue/${track.id}.mp3"),
                java.io.File(context.cacheDir, "lan_uploads/${track.id}.mp3")
            )
            targetFile = candidates.firstOrNull { it.exists() && it.isFile }
        }

        if (targetFile != null && targetFile.exists()) {
            val totalLength = targetFile.length()
            val mimeType = when (targetFile.extension.lowercase()) {
                "flac" -> "audio/flac"
                "wav" -> "audio/wav"
                "m4a", "aac" -> "audio/mp4"
                "ogg" -> "audio/ogg"
                else -> "audio/mpeg"
            }

            val rangeHeader = headers["range"]
            if (!rangeHeader.isNullOrBlank() && rangeHeader.startsWith("bytes=")) {
                val rangeSpec = rangeHeader.removePrefix("bytes=").trim()
                val parts = rangeSpec.split("-")
                val start = parts[0].toLongOrNull() ?: 0L
                val end = if (parts.size > 1 && parts[1].isNotBlank()) {
                    parts[1].toLongOrNull() ?: (totalLength - 1)
                } else {
                    totalLength - 1
                }.coerceAtMost(totalLength - 1)

                val contentLength = (end - start + 1).coerceAtLeast(0)

                val header = "HTTP/1.1 206 Partial Content\r\n" +
                        "Content-Type: $mimeType\r\n" +
                        "Accept-Ranges: bytes\r\n" +
                        "Content-Range: bytes $start-$end/$totalLength\r\n" +
                        "Content-Length: $contentLength\r\n" +
                        "Access-Control-Allow-Origin: *\r\n\r\n"
                output.write(header.toByteArray(Charsets.UTF_8))

                val fis = java.io.FileInputStream(targetFile)
                fis.skip(start)
                val buf = ByteArray(16384)
                var remaining = contentLength
                while (remaining > 0) {
                    val toRead = remaining.coerceAtMost(buf.size.toLong()).toInt()
                    val read = fis.read(buf, 0, toRead)
                    if (read <= 0) break
                    output.write(buf, 0, read)
                    remaining -= read
                }
                fis.close()
            } else {
                val header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: $mimeType\r\n" +
                        "Accept-Ranges: bytes\r\n" +
                        "Content-Length: $totalLength\r\n" +
                        "Access-Control-Allow-Origin: *\r\n\r\n"
                output.write(header.toByteArray(Charsets.UTF_8))

                val fis = java.io.FileInputStream(targetFile)
                val buf = ByteArray(16384)
                var read: Int
                while (fis.read(buf).also { read = it } > 0) {
                    output.write(buf, 0, read)
                }
                fis.close()
            }
        } else if (track.streamUrl.isNotBlank()) {
            val redirect = "HTTP/1.1 302 Found\r\n" +
                    "Location: ${track.streamUrl}\r\n" +
                    "Access-Control-Allow-Origin: *\r\n\r\n"
            output.write(redirect.toByteArray(Charsets.UTF_8))
        } else {
            val notFound = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "Content-Length: 53\r\n\r\n" +
                    "{\"error\":\"Track audio not available locally for stream\"}"
            output.write(notFound.toByteArray(Charsets.UTF_8))
        }
    }

    fun cleanupLanQueueDirectory(dir: java.io.File, maxTotalBytes: Long = 100L * 1024L * 1024L) {
        if (!dir.exists() || !dir.isDirectory) return
        val files = dir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }
        if (totalSize > maxTotalBytes || files.size > 20) {
            val sorted = files.sortedBy { it.lastModified() }
            for (f in sorted) {
                val len = f.length()
                if (f.delete()) {
                    totalSize -= len
                }
                if (totalSize <= (maxTotalBytes * 0.7).toLong() && (dir.listFiles()?.size ?: 0) <= 15) break
            }
        }
    }

    fun generateWebCompanionHtml(): String {
        return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>GAMA • Web Remote & LAN Stream</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
    body { background: #0a0a0f; color: #f1f5f9; min-height: 100vh; display: flex; flex-direction: column; align-items: center; padding: 20px 16px; }
    .card { background: #13131f; border: 1px solid rgba(0,245,255,0.25); border-radius: 20px; width: 100%; max-width: 440px; padding: 24px; box-shadow: 0 10px 30px rgba(0,0,0,0.6); }
    .badge { font-size: 10px; font-family: monospace; padding: 3px 8px; border-radius: 6px; background: rgba(0,245,255,0.15); color: #00f5ff; border: 1px solid rgba(0,245,255,0.3); display: inline-block; }
    .title { font-size: 18px; font-weight: 700; margin-top: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .artist { font-size: 13px; color: #94a3b8; margin-top: 2px; }
    #viz { width: 100%; height: 60px; margin: 16px 0; border-radius: 10px; background: rgba(0,0,0,0.3); }
    .progress-bar { width: 100%; height: 6px; background: #1e1e2f; border-radius: 3px; position: relative; margin: 12px 0 6px; cursor: pointer; }
    .progress-fill { height: 100%; background: linear-gradient(90deg, #00f5ff, #a855f7); border-radius: 3px; width: 0%; }
    .time-row { display: flex; justify-content: space-between; font-size: 11px; font-family: monospace; color: #64748b; }
    .controls { display: flex; justify-content: space-around; align-items: center; margin: 18px 0 12px; }
    .btn { background: #1e1e2f; border: 1px solid #334155; color: #f8fafc; border-radius: 50%; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 16px; transition: all 0.2s; }
    .btn:active { transform: scale(0.92); }
    .btn-main { background: #00f5ff; color: #000; width: 56px; height: 56px; font-size: 20px; font-weight: bold; border: none; box-shadow: 0 0 18px rgba(0,245,255,0.4); }
    .section { margin-top: 16px; padding-top: 14px; border-top: 1px solid rgba(255,255,255,0.08); }
    .row { display: flex; gap: 8px; margin-top: 8px; }
    input[type=text] { background: #0a0a0f; border: 1px solid #334155; border-radius: 10px; padding: 8px 12px; color: #fff; font-size: 13px; width: 100%; }
    .action-btn { background: rgba(0,245,255,0.15); border: 1px solid rgba(0,245,255,0.4); color: #00f5ff; padding: 8px 14px; border-radius: 10px; font-size: 12px; font-weight: 600; cursor: pointer; white-space: nowrap; }
    .status-pill { font-size: 11px; font-family: monospace; color: #94a3b8; text-align: center; margin-bottom: 12px; }
    .toggle-stream { background: #1e1e2f; border: 1px solid #334155; padding: 10px 14px; border-radius: 12px; font-size: 12px; width: 100%; display: flex; justify-content: space-between; align-items: center; color: #e2e8f0; cursor: pointer; }
    .toggle-stream.active { border-color: #00f5ff; background: rgba(0,245,255,0.1); color: #00f5ff; font-weight: bold; }
  </style>
</head>
<body>
  <div class="card">
    <div style="display:flex; justify-content:space-between; align-items:center;">
      <span class="badge" id="qualityBadge">GAMA LAN ENGINE</span>
      <span style="font-size:11px; color:#64748b; font-family:monospace;" id="devModel">Android</span>
    </div>
    <div class="status-pill" id="statusText">Connecting...</div>

    <div class="title" id="trackTitle">GAMA Audio Stream</div>
    <div class="artist" id="trackArtist">Waiting for telemetry...</div>

    <canvas id="viz" width="400" height="60"></canvas>

    <div class="progress-bar" id="progressBar" onclick="seekClick(event)">
      <div class="progress-fill" id="progressFill"></div>
    </div>
    <div class="time-row">
      <span id="posText">0:00</span>
      <span id="durText">0:00</span>
    </div>

    <div class="controls">
      <button class="btn" onclick="sendCmd('prev')">⏮</button>
      <button class="btn btn-main" id="playBtn" onclick="sendCmd('play_pause')">▶</button>
      <button class="btn" onclick="sendCmd('next')">⏭</button>
    </div>

    <div class="section">
      <button class="toggle-stream" id="streamToggleBtn" onclick="toggleStreamAudio()">
        <span>🔊 Stream Audio to this Device</span>
        <span id="streamIndicator" style="font-size:10px;">OFF</span>
      </button>
      <audio id="streamAudio" preload="none"></audio>
    </div>

    <div class="section">
      <div style="font-size:11px; font-family:monospace; color:#94a3b8;">PAIRING PIN AUTHENTICATION</div>
      <div class="row">
        <input type="text" id="pinInput" placeholder="Enter 4-digit PIN" maxlength="4" style="text-align:center; letter-spacing:4px;">
        <button class="action-btn" onclick="pairWithPin()">Pair</button>
      </div>
    </div>

    <div class="section">
      <div style="font-size:11px; font-family:monospace; color:#94a3b8;">SEND SONG TO PHONE QUEUE</div>
      <div class="row">
        <input type="text" id="queueInput" placeholder="YouTube Video ID or Title">
        <button class="action-btn" onclick="pushToPhone()">+ Queue</button>
      </div>
    </div>
  </div>

  <script>
    let ws = null;
    let token = localStorage.getItem('gama_token') || '';
    let isPlaying = false;
    let posMs = 0;
    let durMs = 0;
    let streamActive = false;

    const canvas = document.getElementById('viz');
    const ctx = canvas.getContext('2d');
    let bands = new Array(16).fill(0.1);

    function drawViz() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      const barWidth = (canvas.width / 16) - 3;
      for (let i = 0; i < 16; i++) {
        const h = Math.max(4, bands[i] * canvas.height * 0.9);
        const grad = ctx.createLinearGradient(0, canvas.height, 0, canvas.height - h);
        grad.addColorStop(0, '#00f5ff');
        grad.addColorStop(1, '#a855f7');
        ctx.fillStyle = grad;
        ctx.fillRect(i * (barWidth + 3), canvas.height - h, barWidth, h);
      }
      requestAnimationFrame(drawViz);
    }
    drawViz();

    function formatTime(s) {
      const m = Math.floor(s / 60);
      const sec = Math.floor(s % 60);
      return m + ':' + (sec < 10 ? '0' : '') + sec;
    }

    async function pairWithPin() {
      const pin = document.getElementById('pinInput').value.trim();
      if (!pin) return;
      try {
        const res = await fetch('/pair', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pin, client: 'Mobile Web Companion' })
        });
        if (res.ok) {
          const data = await res.json();
          token = data.token;
          localStorage.setItem('gama_token', token);
          document.getElementById('statusText').innerText = 'Paired! Connecting WebSocket...';
          connectWs();
        } else {
          document.getElementById('statusText').innerText = 'Invalid PIN (HTTP 401)';
        }
      } catch (e) {
        document.getElementById('statusText').innerText = 'Pair error: ' + e.message;
      }
    }

    function connectWs() {
      if (ws) ws.close();
      const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = proto + '//' + location.host + '/ws?token=' + encodeURIComponent(token);
      ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        document.getElementById('statusText').innerText = 'Connected (Live Telemetry)';
        document.getElementById('statusText').style.color = '#34d399';
      };
      ws.onmessage = (e) => {
        try {
          const d = JSON.parse(e.data);
          if (d.type === 'STATUS') {
            isPlaying = d.isPlaying;
            document.getElementById('playBtn').innerText = isPlaying ? '⏸' : '▶';
            posMs = d.positionMs || 0;
            durMs = d.durationMs || 0;
            document.getElementById('posText').innerText = formatTime(posMs / 1000);
            document.getElementById('durText').innerText = formatTime(durMs / 1000);
            if (durMs > 0) {
              document.getElementById('progressFill').style.width = ((posMs / durMs) * 100) + '%';
            }
            if (d.track) {
              document.getElementById('trackTitle').innerText = d.track.title || 'Unknown Title';
              document.getElementById('trackArtist').innerText = d.track.artist || 'Unknown Artist';
              if (d.track.audioQualityBadge) document.getElementById('qualityBadge').innerText = d.track.audioQualityBadge;
            }
            if (d.deviceModel) document.getElementById('devModel').innerText = d.deviceModel;
            if (d.visualizerBands && d.visualizerBands.length) {
              bands = d.visualizerBands;
            }
            if (streamActive) {
              const audio = document.getElementById('streamAudio');
              if (isPlaying && audio.paused) audio.play().catch(() => {});
              if (!isPlaying && !audio.paused) audio.pause();
            }
          }
        } catch (_) {}
      };
      ws.onclose = () => {
        document.getElementById('statusText').innerText = 'Disconnected. Enter PIN to reconnect.';
        document.getElementById('statusText').style.color = '#fbbf24';
      };
    }

    function sendCmd(action, payload = {}) {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ action, ...payload }));
      } else {
        fetch('/command', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
          body: JSON.stringify({ action, ...payload })
        }).catch(() => {});
      }
    }

    function seekClick(e) {
      const rect = e.currentTarget.getBoundingClientRect();
      const pct = (e.clientX - rect.left) / rect.width;
      if (durMs > 0) {
        sendCmd('seek', { positionMs: Math.floor(pct * durMs) });
      }
    }

    function toggleStreamAudio() {
      const audio = document.getElementById('streamAudio');
      const btn = document.getElementById('streamToggleBtn');
      const ind = document.getElementById('streamIndicator');
      streamActive = !streamActive;
      if (streamActive) {
        audio.src = '/stream?token=' + encodeURIComponent(token);
        audio.play().catch(() => {});
        btn.classList.add('active');
        ind.innerText = 'PLAYING';
      } else {
        audio.pause();
        audio.src = '';
        btn.classList.remove('active');
        ind.innerText = 'OFF';
      }
    }

    async function pushToPhone() {
      const input = document.getElementById('queueInput');
      const val = input.value.trim();
      if (!val) return;
      await fetch('/queue/push?token=' + encodeURIComponent(token), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
        body: JSON.stringify({
          title: val,
          artist: 'Mobile Web Companion',
          youtubeVideoId: val.length === 11 ? val : '',
          playNow: false
        })
      });
      input.value = '';
      alert('Pushed to phone queue!');
    }

    if (token) {
      connectWs();
    } else {
      document.getElementById('statusText').innerText = 'Enter 4-digit PIN below to connect';
    }
  </script>
</body>
</html>"""
    }

    private fun resolveLocalIp(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    (ipInt shr 8) and 0xff,
                    (ipInt shr 16) and 0xff,
                    (ipInt shr 24) and 0xff
                )
            }

            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }
}
