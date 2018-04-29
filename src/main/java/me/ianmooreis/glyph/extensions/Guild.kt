package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.DatabaseOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerConfig
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
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

fun Guild.getInfoEmbed(title: String?, footer: String?, color: Color?, showExactCreationDate: Boolean = false): MessageEmbed {
        val createdAgo = PrettyTime().format(this.creationTime.toDate())
        return EmbedBuilder().setTitle(title)
                .addField("Overview", "**Name** ${this.name}\n" +
                        "**ID** ${this.id}\n" +
                        "**Region** ${this.regionRaw}\n" +
                        "**Created** $createdAgo ${if (showExactCreationDate) "(${this.creationTime})" else ""}\n" +
                        "**Owner** ${this.owner.asMention}", false)
                .addField("Members", "**Humans** ${this.members.count { !it.user.isBot }}\n" +
                        "**Bots** ${this.members.count { it.user.isBot }}\n" +
                        "**Online** ${this.members.count { it.onlineStatus == OnlineStatus.ONLINE }}\n" +
                        "**Total** ${this.members.count()}", true)
                .addField("Channels", "**Text** ${this.textChannels.size}\n" +
                        "**Voice** ${this.voiceChannels.size}\n" +
                        "**Categories** ${this.categories.size}", true)
                .addField("Roles", "**Total** ${this.roles.size}\n" +
                        "**List** ${this.roles.joinToString { it.asMention }}", true)
                .setThumbnail(this.iconUrl)
                .setFooter(footer, null)
                .setColor(color)
                .setTimestamp(Instant.now())
                .build()
    }