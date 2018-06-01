package me.ianmooreis.glyph.skills.configuration

import ai.api.model.AIResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.log
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerConfig
import me.ianmooreis.glyph.orchestrators.messaging.CustomEmote
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.Instant


object ServerConfigSetSkill : SkillAdapter("skill.configuration.load", cooldownTime = 4, guildOnly = true, requiredPermissionsUser = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val key = ai.result.getStringParameter("url").split("/").last()
        val url = "https://hastebin.com/raw/$key"
        url.httpGet().responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val data = result.get()
                    val config = parseYAML(data) { updateError(event, it) }
                    if (config != null) {
                        DatabaseOrchestrator.setServerConfig(event.guild, config, { updateSuccess(event) }, { updateError(event, it) })
                        this.log.info("Got ${event.guild} config from $url")
                    }
                }
                is Result.Failure -> {
                    event.message.reply("${CustomEmote.XMARK} An error occurred while try to retrieve a config from the given URL `$url`! Check your url or try waiting a bit before retrying.")
                    this.log.error("Hastebin has thrown a ${response.statusCode} error when trying to get config for ${event.guild}!")
                    event.jda.selfUser.log("Hastebin", "${response.statusCode} error when trying to get config for ${event.guild} with $url!")
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
                    "Please check that you have a properly formatted YAML and the data is as expected!\n" +
                    "```${exception.message?.split("\n")?.first()?.trim()}```\n" +
                    "**Help:** [Documentation](https://glyph-discord.readthedocs.io/en/latest/configuration.html) - " +
                    "[Official Glyph Server](https://discord.me/glyph-discord)")
            .setColor(Color.RED)
            .setFooter("Configuration", null)
            .setTimestamp(Instant.now())
            .build())
    }

    private fun parseYAML(yaml: String, onFailure: (e: Exception) -> Unit): ServerConfig? {
        try {
            return YAMLMapper()
                    .registerKotlinModule()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .readValue(yaml, ServerConfig::class.java)
        } catch (e: Exception) {
            onFailure(e)
        }
        return null
    }
}