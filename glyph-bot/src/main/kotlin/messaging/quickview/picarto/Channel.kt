package org.yttr.glyph.bot.messaging.quickview.picarto

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.yttr.glyph.bot.directors.messaging.SimpleDescriptionBuilder
import java.awt.Color
import java.time.Instant

/**
 * A Picarto channel
 */
@Serializable
data class Channel(
    /**
     * The channel name
     */
    val name: String,
    private val avatar: String,
    private val viewers: Int,
    private val followers: Int,
    private val category: List<String>,
    private val title: String,
    private val online: Boolean,
    private val adult: Boolean,
    private val tags: List<String>
) {

    /**
     * Creates an embed with the channel's info
     */
    fun getEmbed(): MessageEmbed {
        val url = "https://picarto.tv/$name"
        val description = SimpleDescriptionBuilder()
            .addField("Status", if (online) "Online" else "Offline")
            .addField("Category", "${category.joinToString()} (${if (adult) "NSFW" else "SFW"})")
            .addField(null, "**Viewers** $viewers | **Followers** $followers")
            .build()
        return EmbedBuilder()
            .setTitle(this.title, url)
            .setAuthor(this.name, url)
            .setDescription(description)
            .addField("Tags", tags.joinToString(), false)
            .setThumbnail(avatar)
            .setColor(if (online) Color.GREEN else Color.RED)
            .setFooter("picarto", null)
            .setTimestamp(Instant.now())
            .build()
    }
}
