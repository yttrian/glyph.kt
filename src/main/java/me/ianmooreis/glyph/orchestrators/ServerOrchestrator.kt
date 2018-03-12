package me.ianmooreis.glyph.orchestrators

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.audit
import me.ianmooreis.glyph.extensions.deleteConfig
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
        event.guild.audit(getGuildEmbed(event.guild).setTitle("Guild Joined").setColor(Color.GREEN).build())
        log.info("Joined ${event.guild}")
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        updateServerCount(event.jda)
        event.guild.audit(getGuildEmbed(event.guild).setTitle("Guild Left").setColor(Color.RED).build())
        event.guild.deleteConfig()
        log.info("Left ${event.guild}")
    }

    private fun updateServerCount(jda: JDA) {
        val id = jda.selfUser.id
        val count = jda.guilds.count()
        val countJSON = JSONObject().put("server_count", count).toString()
        "https://discordbots.org/api/bots/$id/stats".httpPost().header("Authorization" to System.getenv("DISCORDBOTLIST_TOKEN"), "Content-Type" to "application/json")
                .body(countJSON).responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    log.info("Updated Discord Bot List server count with $count.")
                }
                is Result.Failure -> {
                    log.warn("Failed to update Discord Bot List server count with ${response.statusCode} error!")
                }
            }
        }
        "https://bots.discord.pw/api/bots/$id/stats".httpPost().header("Authorization" to System.getenv("DISCORDBOTS_TOKEN"), "Content-Type" to "application/json")
                .body(countJSON).responseString { _, response, result ->
             when (result) {
                    is Result.Success -> {
                        log.info("Updated Discord Bots server count with $count.")
                    }
                    is Result.Failure -> {
                        log.warn("Failed to update Discord Bots server count with ${response.statusCode} error!")
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