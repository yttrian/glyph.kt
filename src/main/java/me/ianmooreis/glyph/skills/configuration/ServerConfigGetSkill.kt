package me.ianmooreis.glyph.skills.configuration

import ai.api.model.AIResponse
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.log
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.CustomEmote
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.toJSON
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONObject
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
                    event.jda.selfUser.log("Hastebin", "${response.statusCode} error when trying to post config for ${event.guild}!")
                }
            }
        }
    }
}