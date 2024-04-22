package internal

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.util.*

class DiscordWebSocketClient(serverUri: URI?, private val token: String, private val intents: Int) : WebSocketClient(serverUri) {
    private var heartbeatTimer: Timer? = null

    init {
        startHeartbeat()
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        println("Connected to Discord WebSocket server.")
        identify()
    }

    override fun onMessage(message: String) {
        println("Received message from Discord: $message")
        parseMessage(message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("Connection closed. Code: $code, Reason: $reason")
        stopHeartbeat()
    }

    override fun onError(ex: Exception) {
        System.err.println("Error occurred: $ex")
        stopHeartbeat()
    }

    private fun parseMessage(message: String) {
        try {
            val json = JSONObject(message)
            if (json.has("op")) {
                val op = json.getInt("op")
                when (op) {
                    10 -> handleOp10(json)
                    else -> println("Unhandled operation: $op")
                }
            }
        } catch (e: Exception) {
            System.err.println("Error parsing JSON: " + e.message)
        }
    }

    private fun handleOp10(json: JSONObject) {
        if (json.has("d")) {
            val data = json.getJSONObject("d")
            println("Received heartbeat interval: " + data.getInt("heartbeat_interval"))
        }
    }

    private fun startHeartbeat() {
        heartbeatTimer = Timer()
        heartbeatTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                sendHeartbeat()
            }
        }, 0, 1)
    }

    private fun sendHeartbeat() {
        val heartbeat = JSONObject()
        heartbeat.put("op", 1)
        heartbeat.put("d", System.currentTimeMillis())
        send(heartbeat.toString())
    }

    private fun stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer!!.cancel()
            heartbeatTimer = null
        }
    }

    private fun identify() {
        val identify = JSONObject()
        val properties = JSONObject()
        properties.put("\$os", System.getProperty("os.name"))
        properties.put("\$browser", "Java WebSocket Client")
        properties.put("\$device", "Java")
        identify.put("op", 2)
        identify.put(
            "d", JSONObject()
                .put("intents", intents)
                .put("token", token)
                .put("properties", properties)
        )
        send(identify.toString())
    }
}
