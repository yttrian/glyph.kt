package me.ianmooreis.glyph.orchestrators.messaging.quickview.picarto

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color
import java.net.URL
import java.time.Instant

class Channel(val name: String, private val avatar: URL,
              private val viewers: Int, private val followers: Int, private val category: String, private val title: String,
              private val online: Boolean, private val adult: Boolean,
              private val tags: List<String>) {
    fun getEmbed(): MessageEmbed {
        val url = "https://picarto.tv/$name"
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
                .setFooter("picarto", null)
                .setTimestamp(Instant.now())
                .build()
    }
}