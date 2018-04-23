package me.ianmooreis.glyph.orchestrators

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.deleteConfig
import me.ianmooreis.glyph.extensions.log
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.time.Instant

object ServerOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onReady(event: ReadyEvent) {
        updateServerCount(event.jda)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        updateServerCount(event.jda)
        event.jda.selfUser.log(getGuildEmbed(event.guild).setTitle("Guild Joined").setColor(Color.GREEN).build())
        log.info("Joined ${event.guild}")
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        updateServerCount(event.jda)
        event.jda.selfUser.log(getGuildEmbed(event.guild).setTitle("Guild Left").setColor(Color.RED).build())
        event.guild.deleteConfig()
        log.info("Left ${event.guild}")
    }

    private fun updateServerCount(jda: JDA) {
        val id = jda.selfUser.id
        val count = jda.guilds.count()
        val countJSON = JSONObject().put("server_count", count).put("shard_id", jda.shardInfo.shardId).put("shard_count", jda.shardInfo.shardTotal)
        sendServerCount("https://discordbots.org/api/bots/$id/stats", countJSON, System.getenv("DISCORDBOTLIST_TOKEN"))
        sendServerCount("https://bots.discord.pw/api/bots/$id/stats", countJSON, System.getenv("DISCORDBOTS_TOKEN"))
    }

    private fun sendServerCount(url: String, countJSON: JSONObject, token: String) {
        url.httpPost().header("Authorization" to token, "Content-Type" to "application/json").body(countJSON.toString()).responseString {_, response, result ->
            when (result) {
                is Result.Success -> {
                    log.debug("Updated server count at $url.")
                }
                is Result.Failure -> {
                    log.warn("Failed to update server count at $url with ${response.statusCode} error!")
                }
            }
        }
    }

    private fun getGuildEmbed(guild: Guild): EmbedBuilder {
        return EmbedBuilder().setDescription(
                "**Name** ${guild.name}\n" +
                        "**ID** ${guild.id}\n" +
                        "**Members** ${guild.members.size} (Bots: ${guild.members.count { it.user.isBot }})")
                .setThumbnail(guild.iconUrl)
                .setFooter("Logging", null)
                .setTimestamp(Instant.now())
    }
}