package me.ianmooreis.glyph.orchestrators

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.AIServiceContextBuilder
import ai.api.model.AIRequest
import kotlinx.coroutines.experimental.launch
import me.ianmooreis.glyph.extensions.config
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.utils.quickview.FurAffinity
import me.ianmooreis.glyph.utils.quickview.Picarto
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.util.concurrent.TimeUnit

object MessagingOrchestrator : ListenerAdapter() {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
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

    fun logSendFailure(channel: TextChannel) {
        if (channel.type.isGuild) {
            log.warn("Failed to send message in $channel of ${channel.guild}!")
        } else {
            log.warn("Failed to send message in $channel!.")
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        loadCustomEmotes(event.jda.getGuildById(System.getenv("EMOJI_GUILD")))
        if (event.author.isBot or (event.author == event.jda.selfUser) or event.isWebhookMessage) return
        val config = if (event.channelType.isGuild) event.guild.config else DatabaseOrchestrator.getDefaultServerConfig()
        launch {
            if (config.quickview.furaffinityEnabled) {
                FurAffinity.makeQuickviews(event)
            }
            if (config.quickview.picartoEnabled) {
                Picarto.makeQuickviews(event)
            }
        }
        val message: Message = event.message
        if ((!message.isMentioned(event.jda.selfUser) or (message.contentStripped.trim() == message.contentClean)) and event.message.channelType.isGuild) return
        if (event.message.contentClean.isEmpty()) {
            event.message.reply("You have to say something!")
            return
        }
        val ctx = AIServiceContextBuilder().setSessionId("${event.author.id}${event.channel.id}".substring(0..20)).build()
        val ai = DialogFlow.request(AIRequest(event.message.contentClean), ctx)
        if (ai.isError) {
            event.message.reply("It appears DialogFlow is currently unavailable, please try again later!")
            StatusOrchestrator.setStatus(event.jda, OnlineStatus.DO_NOT_DISTURB, Game.watching("temporary outage at DialogFlow"))
            return
        }
        val result = ai.result
        val action = result.action
        launch {
            SkillOrchestrator.trigger(event, ai)
        }
        log.info("Received \"${event.message.contentClean}\" from ${event.author} ${if (event.channelType.isGuild) "in ${event.guild}" else "in PM"}, acted with $action")
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
    XMARK(MessagingOrchestrator.getCustomEmote("xmark")),
    NOMARK(MessagingOrchestrator.getCustomEmote("empty")),
    CHECKMARK(MessagingOrchestrator.getCustomEmote("checkmark")),
    BOT(MessagingOrchestrator.getCustomEmote("bot")),
    DOWNLOAD(MessagingOrchestrator.getCustomEmote("download")),
    DOWNLOADING(MessagingOrchestrator.getCustomEmote("downloading")),
    LOADING(MessagingOrchestrator.getCustomEmote("loading")),
    TYPING(MessagingOrchestrator.getCustomEmote("typing")),
    ONLINE(MessagingOrchestrator.getCustomEmote("online")),
    STREAMING(MessagingOrchestrator.getCustomEmote("streaming")),
    AWAY(MessagingOrchestrator.getCustomEmote("away")),
    DND(MessagingOrchestrator.getCustomEmote("dnd")),
    OFFLINE(MessagingOrchestrator.getCustomEmote("offline")),
    INVISIBLE(MessagingOrchestrator.getCustomEmote("invisible")),
    THINKING(MessagingOrchestrator.getCustomEmote("thinking")),
    COOL(MessagingOrchestrator.getCustomEmote("cool")),
    EXPLICIT(MessagingOrchestrator.getCustomEmote("explicit")),
    CONFIDENTIAL(MessagingOrchestrator.getCustomEmote("confidential")),
    GRIMACE(MessagingOrchestrator.getCustomEmote("grimace")),
    MINDBLOWN(MessagingOrchestrator.getCustomEmote("mindblown"));

    override fun toString() = emote?.asMention ?: ""
}