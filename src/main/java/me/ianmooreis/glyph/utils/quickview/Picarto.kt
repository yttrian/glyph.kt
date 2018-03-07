package me.ianmooreis.glyph.utils.quickview

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.awt.Color
import java.net.URL
import java.time.Instant

class Channel(val name: String, private val avatar: URL,
              private val viewers: Int, private val followers: Int, private val category: String, private val title: String,
              private val online: Boolean, private val adult: Boolean,
              private val tags: List<String>) {
    fun getEmbed(): MessageEmbed {
        val url = "https://picator.tv/$name"
        return EmbedBuilder()
                .setTitle(this.title, url)
                .setAuthor(this.name, url)
                .setDescription(
                        "**Status** ${if (online) "Online" else "Offline"}\n" +
                        "**Category** $category (${if (adult) "NSFW" else "SFW"})\n" +
                        "**Viewers** $viewers | **Followers** $followers")
                .addField("Tags", tags.joinToString(), false)
                .setThumbnail(avatar.toString())
                .setColor(if (online) Color.GREEN else Color.RED)
                .setFooter("Picarto", null)
                .setTimestamp(Instant.now())
                .build()
    }
}

object Picarto {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    fun makeQuickviews(event: MessageReceivedEvent) {
        val urlFormat = Regex("((http[s]?)://)?(www.)?(picarto.tv)/(\\w*)/?", RegexOption.IGNORE_CASE)
        urlFormat.findAll(event.message.contentClean)
                .map { getChannel(it.groups[5]!!.value) }
                .forEach {
                    if (it != null) {
                        event.message.reply(it.getEmbed())
                        log.info("Created Picarto QuickView in ${event.guild} for ${it.name}")
                    }
                }
    }

    private fun getChannel(name: String): Channel? { //TODO: Figure out how not to do it blocking, because async had errors
        val (_, _, result) = "https://api.picarto.tv/v1/channel/name/$name".httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                Gson().fromJson(result.get(), Channel::class.java)
            }
            is Result.Failure -> {
                this.log.warn("Failed to get channel $name from Picarto!")
                return null
            }
        }
    }
}