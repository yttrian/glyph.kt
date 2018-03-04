package me.ianmooreis.glyph.orchestrators

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

object ServerOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    override fun onGuildJoin(event: GuildJoinEvent) {
        updateServerCount(event.jda)
        log.info("Joined ${event.guild}")
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        updateServerCount(event.jda)
        DatabaseOrchestrator.deleteServerConfig(event.guild)
        log.info("Left ${event.guild}")
    }

    fun updateServerCount(jda: JDA) {
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
}