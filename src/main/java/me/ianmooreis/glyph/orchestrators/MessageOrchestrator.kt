package me.ianmooreis.glyph.orchestrators

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import club.minnced.kjda.promise
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
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

    fun amendLedger(invoker: String?, response: String?) {
        if (invoker != null && response != null)
            ledger.put(invoker, response)
    }

    fun getLedgerSize() : Int {
        return ledger.size
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot or (event.author == event.jda.selfUser)) return
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
        SkillOrchestrator.trigger(event, ai)
        log.info("Received \"${event.message.contentClean}\", acted with $action")
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.messageId in ledger) {
            event.channel.getMessageById(ledger[event.messageId]).promise().then {
                it.addReaction("❌").queue()
                it.delete().queueAfter(1, TimeUnit.SECONDS)
                ledger.remove(it.id)
            }
        }
    }
}

fun Message.reply(content: String) {
    this.channel.sendMessage(content).promise().then {
        MessageOrchestrator.amendLedger(this.id, it.id)
    }
}

fun Message.reply(embed: MessageEmbed) {
    this.channel.sendMessage(embed).promise().then {
        MessageOrchestrator.amendLedger(this.id, it.id)
    }
}

val Message.contentClean : String
    get() = if (this.channelType.isGuild) {
        this.contentStripped.removePrefix("@${this.guild.getMember(this.jda.selfUser).effectiveName}").trim()
    } else {
        this.contentStripped.removePrefix("@${this.jda.selfUser.name}").trim()
    }
