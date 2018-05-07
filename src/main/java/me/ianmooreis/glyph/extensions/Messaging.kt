package me.ianmooreis.glyph.extensions

import me.ianmooreis.glyph.orchestrators.messaging.MessagingOrchestrator
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit


fun Message.reply(content: String? = null, embed: MessageEmbed? = null, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS, deleteWithEnabled: Boolean = true) {
    if (content == null && embed == null) { return }
    val message = MessageBuilder().setContent(content?.trim()).setEmbed(embed).build()
    try {
        this.channel.sendMessage(message).queue {
            if (deleteAfterDelay > 0) {
                it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
            } else if (deleteWithEnabled) {
                MessagingOrchestrator.amendLedger(this.idLong, it.idLong)
            }
        }
    } catch (e: InsufficientPermissionException) {
        MessagingOrchestrator.logSendFailure(this.textChannel)
    }
}

fun Message.reply(embed: MessageEmbed, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS, deleteWithEnabled: Boolean = true) {
    this.reply(content = null, embed = embed, deleteAfterDelay = deleteAfterDelay, deleteAfterUnit = deleteAfterUnit, deleteWithEnabled = deleteWithEnabled)
}

val Message.contentClean: String
    get() = if (this.channelType.isGuild) {
        this.contentStripped.removePrefix("@${this.guild.selfMember.effectiveName}").trim()
    } else {
        this.contentStripped.removePrefix("@${this.jda.selfUser.name}").trim()
    }

val Message.cleanMentionedMembers: List<Member>
    get() = this.mentionedMembers.filter { it != this.guild.selfMember }

val Message.cleanMentionedUsers: List<User>
    get() = this.mentionedUsers.filter { it != this.jda.selfUser }

fun TextChannel.getMessagesSince(time: OffsetDateTime, success: (List<Message>) -> Unit) {
    success(this.iterableHistory.takeWhile { it.creationTime.isAfter(time) })
}