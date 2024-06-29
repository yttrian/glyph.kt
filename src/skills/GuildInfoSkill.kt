package org.yttr.glyph.skills

import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.Guild
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.count
import net.dv8tion.jda.api.OnlineStatus
import org.ocpsoft.prettytime.PrettyTime
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.directors.messaging.SimpleDescriptionBuilder
import org.yttr.glyph.extensions.toDate
import java.time.Instant

/**
 * A skill that allows users to ask for different info about a guild
 */
class GuildInfoSkill : Skill("skill.moderation.guildInfo") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
        val guild = event.getGuildOrNull()

        if (guild == null) {
            event.reply { content = "You can only do this in a server!" }
            return
        }

        val messageContent = when (val property = ai.result.getStringParameter("guildProperty")) {
            "name" -> "This guild is **${guild.name}**."
            "id" -> "The id for ${guild.name} is **${guild.id}**."
            "created" -> "${guild.name} was created **${guild.id.timestamp.formatPrettyTime()}** (${guild.id.timestamp})."
            "owner" -> "**${guild.owner.mention}** is the owner of ${guild.name}."
            "members" -> "${guild.name} has **${guild.members.count()}** members."
            "membersHumans" -> "${guild.name} has **${guild.members.count { !it.isBot }}** humans."
            "membersBots" -> "${guild.name} has **${guild.members.count { it.isBot }}** bots."
            "channels" -> "${guild.name} has **${guild.textChannelCount() + guild.voiceChannelCount()}** channels."
            "channelsText" -> "${guild.name} has **${guild.textChannelCount()}** text channels."
            "channelsVoice" -> "${guild.name} has **${guild.voiceChannelCount()}** voice channels."
            "roles" -> "${guild.name} has **${guild.roles.count()}** roles."
            "farm" -> "Servers are no longer checked for bot farming."
            null -> null
            else -> "I'm not sure what property `$property` is for a guild."
        }

        if (messageContent != null) {
            event.reply { content = messageContent }
        } else {
            event.reply { embeds = mutableListOf(guild.getInfoEmbed()) }
        }
    }

    private suspend fun Guild.textChannelCount() = channels.count { it.type == ChannelType.GuildText }
    private suspend fun Guild.voiceChannelCount() = channels.count { it.type == ChannelType.GuildVoice }

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
    private fun Guild.getInfoEmbed(
        title: String? = "Server Info",
        footer: String? = "Moderation",
    ): EmbedBuilder {
        val guild = this

        return EmbedBuilder().apply {
            field("Overview") {
                SimpleDescriptionBuilder {
                    addField(name = "Name", content = guild.name)
                    addField(name = "ID", content = guild.id.toString())
                    addField(
                        name = "Created",
                        content = "${guild.id.timestamp.formatPrettyTime()} (${guild.id.timestamp})"
                    )
                    addField(name = "Owner", content = guild.owner.mention)
                }
            }
        }

        val createdAgo = PrettyTime().format(this.timeCreated.toDate())
        val overviewDescription = SimpleDescriptionBuilder()
            .addField("Name", this.name)
            .addField("ID", this.id)
            .addField("Region", this.regionRaw)
            .addField("Created", "$createdAgo ${if (showExactCreationDate) "(${this.timeCreated})" else ""}")
            .addField("Owner", this.owner?.asMention ?: "?")
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
}
