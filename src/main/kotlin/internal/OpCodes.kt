package internal

object OpCode {
    val dispatch = 0
    val heartbeat = 1
    val identify = 2
    val presence_update = 3
    val voice_state_update = 4
    val resume = 5
    val reconnect = 6
    val request_guild_members = 7
    val invalid_session = 8
    val hello = 10
    val heartbeat_ack = 11
}