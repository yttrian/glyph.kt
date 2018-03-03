package me.ianmooreis.glyph.orchestrators

import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object Server : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        updateServerCount(event.jda.selfUser.id, event.jda.guilds.count())
        println("Joined ${event.guild}")
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        updateServerCount(event.jda.selfUser.id, event.jda.guilds.count())
        DatabaseOrchestrator.deleteServerConfig(event.guild)
        println("Left ${event.guild}")
    }

    private fun updateServerCount(id: String, count: Int) {
        val url = "https://discordbots.org/api/bots/$id/stats"
        val client = OkHttpClient()
        val data = JSONObject().put("server_count", count)
        val body : RequestBody = RequestBody.create(MediaType.parse("application/json"), data.toString())

        val request : Request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", System.getenv("")) //TODO: Add Token
                .build()
        try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}