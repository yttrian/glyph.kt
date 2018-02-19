package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import me.ianmooreis.glyph.orchestrators.*
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONObject
import java.awt.Color
import java.net.URL
import java.time.Instant


object ServerConfigGetSkill : Skill("skill.configuration.view", serverOnly = true, requiredPermissions = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        Fuel.post("https://hastebin.com/documents").body(event.guild.config.toJSON().toString(4)).responseString { _, response, result ->
            if (response.statusCode == 200) {
                val key = JSONObject(result.get()).getString("key")
                val url = URL("https://hastebin.com/$key")
                this.log.info("Posted ${event.guild} config to $url")
                event.message.reply(EmbedBuilder()
                        .setTitle("Configuration Viewer")
                        .setDescription(
                                "Here's the current server config:\n" +
                                "$url\n" +
                                "**Help:** [Documentation](https://glyph-discord.readthedocs.io/en/latest/configuration.html) - " +
                                "[Official Glyph Server](https://discord.me/glyph-discord)")
                        .setFooter("Configuration", null)
                        .setTimestamp(Instant.now())
                        .build())
            } else {
                this.log.error("Hastebin has thrown an error when trying to post config for ${event.guild}!")
            }
        }
    }
}

object ServerConfigSetSkill : Skill("skill.configuration.load", serverOnly = true, requiredPermissions = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val key = ai.result.getStringParameter("url").split("/").last()
        val url = URL("https://hastebin.com/raw/$key")
        Fuel.get(url.toString()).responseString { _, response, result ->
            val data = result.get()
            if (response.statusCode == 200) {
                val config = this.parseJSON(data) { this.updateError(event, it) }
                if (config != null) {
                    DatabaseOrchestrator.setServerConfig(event.guild, config, { updateSuccess(event) }, { this.updateError(event, it) })
                    this.log.info("Got ${event.guild} config from $url")
                }
            } else {
                event.message.reply("${CustomEmote.XMARK} An error occurred while try to retrieve a config from the given URL ($url)!")
                this.log.error("Hastebin has thrown an error when trying to get config for ${event.guild}!")
            }
        }
    }
    private fun updateSuccess(event: MessageReceivedEvent) {
        event.message.reply(EmbedBuilder()
                .setTitle("Configuration Updated")
                .setDescription("The server configuration has been successfully updated!")
                .setFooter("Configuration", null)
                .setTimestamp(Instant.now())
                .build())
    }
    private fun updateError(event: MessageReceivedEvent, exception: Exception) {
        event.message.reply(EmbedBuilder()
            .setTitle("Configuration Error")
            .setDescription(
                    "This servers configuration failed to update for the following reason(s)! " +
                    "Please check that you have a properly formatted JSON and the data is as expected!\n" +
                    "```${exception.cause}```\n" +
                    "**Help:** [Documentation](https://glyph-discord.readthedocs.io/en/latest/configuration.html) - " +
                    "[Official Glyph Server](https://discord.me/glyph-discord)")
            .setColor(Color.RED)
            .setFooter("Configuration", null)
            .setTimestamp(Instant.now())
            .build())
    }
    private fun parseJSON(json: String, onFailure: (e: JsonSyntaxException) -> Unit): ServerConfig? {
        try {
            return Gson().fromJson(json, ServerConfig().javaClass)
        } catch (e: JsonSyntaxException) {
            onFailure(e)
        }
        return null
    }
}