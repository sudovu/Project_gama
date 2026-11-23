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

                                val uploadDir = java.io.File(appContext?.cacheDir, "lan_uploads").apply { mkdirs() }
                                val savedFile = java.io.File(uploadDir, "${System.currentTimeMillis()}_$filename")

                                val bodyChars = CharArray(contentLength)
                                input.read(bodyChars, 0, contentLength)
                                savedFile.writeBytes(String(bodyChars).toByteArray(Charsets.ISO_8859_1))

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
