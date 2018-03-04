package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.*
import me.ianmooreis.glyph.utils.webhooks.LoggingWebhook
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONObject
import java.awt.Color
import java.net.URL
import java.time.Instant


object ServerConfigGetSkill : Skill("skill.configuration.view", serverOnly = true, requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        "https://hastebin.com/documents".httpPost().body(event.guild.config.toJSON()).responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
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
                }
                is Result.Failure -> {
                    event.message.reply("${CustomEmote.XMARK} There was an error trying to post this server's config to Hastebin, please try again later!")
                    this.log.error("Hastebin has thrown a ${response.statusCode} error when trying to post config for ${event.guild}!")
                    LoggingWebhook.log("Hastebin", "${response.statusCode} error when trying to post config for ${event.guild}!", event.jda.selfUser)
                }
            }
        }
    }
}

object ServerConfigSetSkill : Skill("skill.configuration.load", serverOnly = true, requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val key = ai.result.getStringParameter("url").split("/").last()
        val url = "https://hastebin.com/raw/$key"
        url.httpGet().responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val data = result.get()
                    val config = this.parseJSON(data) { this.updateError(event, it) }
                    if (config != null) {
                        DatabaseOrchestrator.setServerConfig(event.guild, config, { updateSuccess(event) }, { this.updateError(event, it) })
                        this.log.info("Got ${event.guild} config from $url")
                    }
                }
                is Result.Failure -> {
                    event.message.reply("${CustomEmote.XMARK} An error occurred while try to retrieve a config from the given URL ($url)! Check your url or try waiting a bit before retrying.")
                    this.log.error("Hastebin has thrown a ${response.statusCode} error when trying to get config for ${event.guild}!")
                    LoggingWebhook.log("Hastebin", "${response.statusCode} error when trying to get config for ${event.guild} with $url!", event.jda.selfUser)
                }
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
                    "```${exception.message?.split("\n")?.first()?.trim()}```\n" +
                    "**Help:** [Documentation](https://glyph-discord.readthedocs.io/en/latest/configuration.html) - " +
                    "[Official Glyph Server](https://discord.me/glyph-discord)")
            .setColor(Color.RED)
            .setFooter("Configuration", null)
            .setTimestamp(Instant.now())
            .build())
    }
    private fun parseJSON(json: String, onFailure: (e: JsonSyntaxException) -> Unit): ServerConfig? {
        try {
            return Gson().fromJson(json, ServerConfig::class.java)
        } catch (e: JsonSyntaxException) {
            onFailure(e)
        }
        return null
    }
}