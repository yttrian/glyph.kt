package org.yttr.glyph.quickviews.picarto

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.yttr.glyph.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.quickviews.GREEN
import org.yttr.glyph.quickviews.RED

/**
 * A Picarto channel
 */
@Serializable
data class Channel(
    /**
     * The channel name
     */
    val name: String,
    val avatar: String,
    val viewers: Int,
    val followers: Int,
    val category: List<String>,
    val title: String,
    val online: Boolean,
    val adult: Boolean,
    val tags: List<String>
)

/**
 * Creates an embed with the channel's info
 */
fun Channel.getEmbed(): EmbedBuilder {
    val channel = this

    return EmbedBuilder().apply {
        title = channel.title
        url = "https://picarto.tv/$name"
        description = SimpleDescriptionBuilder()
            .addField("Status", if (online) "Online" else "Offline")
            .addField("Category", "${category.joinToString()} (${if (adult) "NSFW" else "SFW"})")
            .addField(null, "**Viewers** $viewers | **Followers** $followers")
            .build()
        author {
            name = channel.name
            url = url
        }
        field("Tags") { tags.joinToString() }
        thumbnail {
            url = avatar
        }
        color = if (online) Color.GREEN else Color.RED
        footer {
            text = "Picarto"
        }
        timestamp = Clock.System.now()
    }
}
