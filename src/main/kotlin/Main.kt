import internal.DiscordWebSocketClient
import io.github.cdimascio.dotenv.dotenv
import java.net.URI

suspend fun main(args: Array<String>) {
    val dotenv = dotenv()
    val PATH = "?v=10&encording=json"
    val GATEWAY = "wss://gateway.discord.gg/$PATH"


   try {
       val token = dotenv["TOKEN"]
       val client = DiscordWebSocketClient(URI(GATEWAY), token, 513)
       client.connect()

   } catch(e: Exception) {
       e.printStackTrace()
   }
}