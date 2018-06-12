package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerConfig
import me.ianmooreis.glyph.orchestrators.messaging.SimpleDescriptionBuilder
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import org.ocpsoft.prettytime.PrettyTime
import java.awt.Color
import java.time.Instant

val Guild.config: ServerConfig
    get() = DatabaseOrchestrator.getServerConfig(this)

fun Guild.deleteConfig() {
    DatabaseOrchestrator.deleteServerConfig(this)
}

val Guild.isBotFarm: Boolean
    get() = (botRatio > .8 && members.count() > 10 && !DatabaseOrchestrator.hasCustomConfig(this))

val Guild.botRatio: Float
    get() {
        val members = this.members.count()
        val bots = this.members.count { it.user.isBot }
        return (bots.toFloat() / members.toFloat())
    }

fun Guild.findUser(search: String): User? {
    return this.getMembersByEffectiveName(search, true).firstOrNull()?.user ?:
    this.getMembersByName(search, true).firstOrNull()?.user ?:
    this.getMembersByNickname(search, true).firstOrNull()?.user ?:
    try { this.jda.getUserById(search) } catch (e: NumberFormatException) { null }
}

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