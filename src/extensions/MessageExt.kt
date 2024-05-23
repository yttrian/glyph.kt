package org.yttr.glyph.extensions

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

/**
 * Removes the @mention prefix from a content stripped message and trims any extra whitespace
 */
val Message.contentClean: String
    get() = if (this.channelType.isGuild) {
        this.contentStripped.removePrefix("@${this.guild.selfMember.effectiveName}").trim()
    } else {
        this.contentStripped.removePrefix("@${this.jda.selfUser.name}").trim()
    }

/**
 * Removes the self member from the mentioned members list
 */
val Message.cleanMentionedMembers: List<Member>
    get() = this.mentionedMembers.filter { it != this.guild.selfMember }

/**
 * Removes the self user from the mentioned users list
 */
val Message.cleanMentionedUsers: List<User>
    get() = this.mentionedUsers.filter { it != this.jda.selfUser }
