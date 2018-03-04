package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.MessageOrchestrator
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit


fun Message.reply(content: String, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS) {
    try {
        this.channel.sendMessage(content.trim()).queue {
            if (deleteAfterDelay > 0) {
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            } else {
                MessageOrchestrator.amendLedger(this.id, it.id)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessageOrchestrator.logSendFailure(this.textChannel)
    }
}

fun Message.reply(embed: MessageEmbed, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS) {
    try {
        this.channel.sendMessage(embed).queue {
            if (deleteAfterDelay > 0){
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            } else {
                MessageOrchestrator.amendLedger(this.id, it.id)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessageOrchestrator.logSendFailure(this.textChannel)
    }
}

fun TextChannel.safeSendMessage(content: String, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS){
    try {
        this.sendMessage(content).queue {
            if (deleteAfterDelay > 0){
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessageOrchestrator.logSendFailure(this)
    }
}

fun TextChannel.safeSendMessage(embed: MessageEmbed, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS){
    try {
        this.sendMessage(embed).queue {
            if (deleteAfterDelay > 0){
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessageOrchestrator.logSendFailure(this)
    }
}

val Message.contentClean: String
    get() = if (this.channelType.isGuild) {
        this.contentStripped.removePrefix("@${this.guild.selfMember.effectiveName}").trim()
    } else {
        this.contentStripped.removePrefix("@${this.jda.selfUser.name}").trim()
    }

val Message.cleanMentionedMembers: List<Member>
    get() = this.mentionedMembers.filter { it.user != this.jda.selfUser }

val Message.cleanMentionedUsers: List<User>
    get() = this.mentionedUsers.filter { it != this.jda.selfUser }

fun TextChannel.getMessagesSince(time: OffsetDateTime, success: (List<Message>) -> Unit) {
    success(this.iterableHistory.filter { it.creationTime.isAfter(time) })
}