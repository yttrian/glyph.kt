package me.ianmooreis.glyph.orchestrators

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object MessageOrchestrator : ListenerAdapter() {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private object DialogFlow : AIDataService(AIConfiguration(System.getenv("DIALOGFLOW_TOKEN")))
    private var ledger = mutableMapOf<String, String>()
    private var customEmotes = mapOf<String, Emote>()

    fun getCustomEmote(name: String) : Emote? {
        return customEmotes[name]
    }

    private fun loadCustomEmotes(guild: Guild) {
        if (customEmotes.isEmpty()) {
            customEmotes = guild.emotes.map {
                it.name to it
            }.toMap()
        }
    }

    fun amendLedger(invoker: String?, response: String?) {
        if (invoker != null && response != null)
            ledger[invoker] = response
    }

    fun getLedgerSize() : Int {
        return ledger.size
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        this.loadCustomEmotes(event.jda.getGuildById(System.getenv("HOME_GUILD")))
        if (event.author.isBot or (event.author == event.jda.selfUser) or event.isWebhookMessage) return
        //TODO: Add QuickView
        val message: Message = event.message
        if ((!message.isMentioned(event.jda.selfUser) or (message.contentStripped.trim() == message.contentClean)) and event.message.channelType.isGuild) return
        val ai = DialogFlow.request(AIRequest(event.message.contentClean))
        if (ai.isError) {
            event.message.reply("It appears DialogFlow is currently unavailable, please try again later!")
            return
        }
        val result = ai.result
        val action = result.action
        launch {
            SkillOrchestrator.trigger(event, ai)
        }
        log.info("Received \"${event.message.contentClean}\", acted with $action")
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.messageId in ledger) {
            event.channel.getMessageById(ledger[event.messageId]).queue {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
                ledger.remove(it.id)
            }
        }
    }
}

enum class CustomEmote(val emote: Emote?) {
    XMARK(MessageOrchestrator.getCustomEmote("xmark")),
    NOMARK(MessageOrchestrator.getCustomEmote("empty")),
    CHECKMARK(MessageOrchestrator.getCustomEmote("checkmark")),
    BOT(MessageOrchestrator.getCustomEmote("bot")),
    DOWNLOAD(MessageOrchestrator.getCustomEmote("download")),
    DOWNLOADING(MessageOrchestrator.getCustomEmote("downloading")),
    LOADING(MessageOrchestrator.getCustomEmote("loading")),
    TYPING(MessageOrchestrator.getCustomEmote("typing")),
    ONLINE(MessageOrchestrator.getCustomEmote("online")),
    STREAMING(MessageOrchestrator.getCustomEmote("streaming")),
    AWAY(MessageOrchestrator.getCustomEmote("away")),
    DND(MessageOrchestrator.getCustomEmote("dnd")),
    OFFLINE(MessageOrchestrator.getCustomEmote("offline")),
    INVISIBLE(MessageOrchestrator.getCustomEmote("invisible")),
    THINKING(MessageOrchestrator.getCustomEmote("thinking")),
    COOL(MessageOrchestrator.getCustomEmote("cool")),
    EXPLICIT(MessageOrchestrator.getCustomEmote("explicit")),
    CONFIDENTIAL(MessageOrchestrator.getCustomEmote("confidential")),
    GRIMACE(MessageOrchestrator.getCustomEmote("grimace")),
    MINDBLOWN(MessageOrchestrator.getCustomEmote("mindblown"));

    override fun toString() = emote?.asMention ?: ""
}

fun Message.reply(content: String, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS) {
    this.channel.sendMessage(content.trim()).queue {
        if (deleteAfterDelay > 0){
            it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
        } else {
            MessageOrchestrator.amendLedger(this.id, it.id)
        }
    }
}

fun Message.reply(embed: MessageEmbed, deleteAfterDelay: Long = 0, deleteAfterUnit: TimeUnit = TimeUnit.SECONDS) {
    this.channel.sendMessage(embed).queue {
        if (deleteAfterDelay > 0){
            it.delete().queueAfter(deleteAfterDelay, deleteAfterUnit)
        } else {
            MessageOrchestrator.amendLedger(this.id, it.id)
        }
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

fun TextChannel.getMessagesSince(time: OffsetDateTime) = this.iterableHistory.filter { it.creationTime.isAfter(time) }

fun OffsetDateTime.toDate(): Date = Date.from(this.toInstant())