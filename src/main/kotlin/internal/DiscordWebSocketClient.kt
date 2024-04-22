package internal

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.util.*


class DiscordWebSocketClient(serverUri: URI?, private val token: String, private val intents: Int) : WebSocketClient(serverUri) {
    private var heartbeatTimer: Timer? = null

    override fun onOpen(handshakedata: ServerHandshake) {
        println("Connected to Discord WebSocket server.")
        identify()
       //  startHeartbeat("1")
    }


    private fun startHeartbeat(heartbeatInterval: String) {
        heartbeatTimer = Timer()
        heartbeatTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                sendHeartbeat()
            }
        }, 1, heartbeatInterval.toLong())
    }

    private fun sendHeartbeat() {
        val heartbeat = JSONObject()
        heartbeat.put("op", OpCode.heartbeat)
        heartbeat.put("d", "null") // No data needed for heartbeat
        send(heartbeat.toString())
    }

    private fun stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer!!.cancel()
            heartbeatTimer = null
        }
    }


    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        TODO("Not yet implemented")
        stopHeartbeat()
    }


    override fun onMessage(message: String) {
        println("Received message from Discord: $message")
        parseMessage(message)
    }
    override fun onError(ex: Exception) {
        System.err.println("Error occurred: $ex")
        stopHeartbeat()
    }

    fun triggeredEvent(op: Int) {
        println("$op was triggered")
    }

    private fun parseMessage(message: String) {
        try {
            val json = JSONObject(message)
            if (json.has("op")) {
                val op = json.getInt("op")
                when (op) {
                    OpCode.hello -> handleOp10(json)
                    OpCode.dispatch -> triggeredEvent(op)
                    OpCode.heartbeat_ack -> triggeredEvent(op)
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

    private fun identify() {
        val identify = JSONObject()
        val properties = JSONObject()
        properties.put("\$os", System.getProperty("os.name"))
        properties.put("\$browser", "Java WebSocket Client")
        properties.put("\$device", "Java")
        identify.put("op", OpCode.identify)
        identify.put(
            "d", JSONObject()
                .put("intents", intents)
                .put("token", token)
                .put("properties", properties)
        )
        send(identify.toString())
    }
}
