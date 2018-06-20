package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.configs.ServerConfig
import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

/**
 * The configuration of a guild (either custom or default if no custom one found)
 */
val Guild.config: ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

/**
 * Delete a guild's configuration from the database
 */
fun Guild.deleteConfig() {
    DatabaseOrchestrator.deleteServerConfig(this)
}

/**
 * Whether or not the guild is considered a bot farm
 */
val Guild.isBotFarm: Boolean
    get() = (botRatio > .8 && members.count() > 10 && !DatabaseOrchestrator.hasCustomConfig(this))

/**
 * The ratio of bots to humans as a percentage
 */
val Guild.botRatio: Float
    get() {
        val members = this.members.count()
        val bots = this.members.count { it.user.isBot }
        return (bots.toFloat() / members.toFloat())
    }

/**
 * Attempt to find a user in a guild by their effective name, username, nickname, and/or id
 *
 * @param search the value to use to try and find a user
 *
 * @return a user or null if not found
 */
fun Guild.findUser(search: String): User? {
    return this.getMembersByEffectiveName(search, true).firstOrNull()?.user
        ?: this.getMembersByName(search, true).firstOrNull()?.user
        ?: this.getMembersByNickname(search, true).firstOrNull()?.user ?: try {
            this.jda.getUserById(search)
        } catch (e: NumberFormatException) {
            null
        }
}

/**
 * Get an informational embed about a server
 *
 * @param title  the title of the embed
 * @param footer any footer text to include in the embed
 * @param color  the color of the embed
 * @param showExactCreationDate whether or not to show the exact timestamp for the server creation time
 *
 * @return an embed with the requested server info
 */
fun Guild.getInfoEmbed(title: String?, footer: String?, color: Color?, showExactCreationDate: Boolean = false): MessageEmbed {
    val createdAgo = PrettyTime().format(this.creationTime.toDate())
    val overviewDescription = SimpleDescriptionBuilder()
        .addField("Name", this.name)
        .addField("ID", this.id)
        .addField("Region", this.regionRaw)
        .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.creationTime})" else ""}")
        .addField("Owner", this.owner.asMention)
        .build()
    val membersDescription = SimpleDescriptionBuilder()
        .addField("Humans", this.members.count { !it.user.isBot })
        .addField("Bots", this.members.count { it.user.isBot })
        .addField("Online", this.members.count { it.onlineStatus == OnlineStatus.ONLINE })
        .addField("Total", this.members.count())
        .build()
    val channelsDescription = SimpleDescriptionBuilder()
        .addField("Text", this.textChannels.count())
        .addField("Voice", this.voiceChannels.count())
        .addField("Categories", this.categories.count())
        .build()
    val rolesDescription = SimpleDescriptionBuilder()
        .addField("Total", this.roles.count())
        .addField("List", this.roles.joinToString { it.asMention })
        .build()
    return EmbedBuilder().setTitle(title)
        .addField("Overview", overviewDescription, false)
        .addField("Members", membersDescription, true)
        .addField("Channels", channelsDescription, true)
        .addField("Roles", rolesDescription, true)
        .setThumbnail(this.iconUrl)
        .setFooter(footer, null)
        .setColor(color)
        .setTimestamp(Instant.now())
        .build()
}