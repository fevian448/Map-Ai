package com.example.mapai.data.remote

import android.util.Log
import com.example.mapai.data.AlertType
import com.example.mapai.data.GeoPoint
import com.example.mapai.data.TrafficAlert
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URI

object SocketManager {
    private const val TAG = "SocketManager"
    private var socket: Socket? = null
    private val gson = Gson()

    var onNewReport: ((TrafficAlert) -> Unit)? = null
    var onNewSos: ((TrafficAlert) -> Unit)? = null
    var onNewPlace: ((PlaceDto) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    fun connect(serverUrl: String) {
        if (socket?.connected() == true) return

        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Integer.MAX_VALUE
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                transports = arrayOf("websocket")
                timeout = 10000
            }

            val uri = URI.create(serverUrl)
            socket = IO.socket(uri, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                onConnectionChange?.invoke(true)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                onConnectionChange?.invoke(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connect error: ${args.joinToString()}")
            }

            socket?.on("report:new") { args ->
                try {
                    val json = args.firstOrNull() as? JSONObject ?: return@on
                    val typeStr = json.optString("type", "")
                    val alertType = AlertType.values().find { it.name == typeStr } ?: AlertType.HAZARD
                    val alert = TrafficAlert(
                        id = json.optString("id"),
                        type = alertType,
                        point = GeoPoint(json.optDouble("lat"), json.optDouble("lon")),
                        description = json.optString("description"),
                        reporter = json.optString("reporter"),
                        timestamp = json.optLong("created_at", System.currentTimeMillis()),
                        confidence = json.optInt("confidence", 100),
                        confirmedBy = json.optInt("confirmed", 1)
                    )
                    onNewReport?.invoke(alert)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing report:new", e)
                }
            }

            socket?.on("sos:new") { args ->
                try {
                    val json = args.firstOrNull() as? JSONObject ?: return@on
                    val alert = TrafficAlert(
                        id = json.optString("id"),
                        type = AlertType.HAZARD,
                        point = GeoPoint(json.optDouble("lat"), json.optDouble("lon")),
                        description = json.optString("message", "SOS"),
                        reporter = json.optString("user", "anon"),
                        timestamp = System.currentTimeMillis(),
                        confidence = 100,
                        confirmedBy = 1
                    )
                    onNewSos?.invoke(alert)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sos:new", e)
                }
            }

            socket?.on("place:new") { args ->
                try {
                    val json = args.firstOrNull() as? JSONObject ?: return@on
                    val place = gson.fromJson(json.toString(), PlaceDto::class.java)
                    onNewPlace?.invoke(place)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing place:new", e)
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection failed", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.close()
        socket = null
    }

    fun isConnected(): Boolean = socket?.connected() == true
}
