package me.ianmooreis.glyph.orchestrators

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import kotlinx.coroutines.experimental.launch
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

    fun logSendFailure(channel: TextChannel) {
        if (channel.type.isGuild) {
            this.log.warn("Failed to send message in $channel of ${channel.guild}!")
        } else {
            this.log.warn("Failed to send message in $channel!.")
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        this.loadCustomEmotes(event.jda.getGuildById(System.getenv("HOME_GUILD")))
        if (event.author.isBot or (event.author == event.jda.selfUser) or event.isWebhookMessage) return
        val config = if (event.channelType.isGuild) event.guild.config else DatabaseOrchestrator.getDefaultServerConfig()
        if (config.faQuickviewEnabled) {
            FurAffinity.makeQuickviews(event)
        }
        if (config.picartoQuickviewEnabled) {
            Picarto.makeQuickviews(event)
        }
        if (config.spoilersKeywords.intersect(event.message.contentClean.split(" ")).isNotEmpty() && event.message.textChannel.name != config.spoilersChannel) {
            log.info("Marked \"${event.message}\" in ${event.guild} as spoiler.")
            event.message.addReaction("⚠").queue()
        }
        val message: Message = event.message
        if ((!message.isMentioned(event.jda.selfUser) or (message.contentStripped.trim() == message.contentClean)) and event.message.channelType.isGuild) return
        val ai = DialogFlow.request(AIRequest(event.message.contentClean))
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