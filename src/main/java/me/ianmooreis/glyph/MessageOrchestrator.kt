package me.ianmooreis.glyph

import ai.api.AIConfiguration
import ai.api.AIDataService
import ai.api.model.AIRequest
import me.ianmooreis.glyph.skills.helpSkill
import me.ianmooreis.glyph.skills.statusSkill
import net.dv8tion.jda.core.entities.Emote
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
        if (event.author.isBot) return
        val ai = DialogFlow.request(AIRequest(event.message.strippedContent))
        if (ai.isError) {
            event.message.channel.sendMessage("It appears DialogFlow is currently unavailable, please try again later!").queue()
            return
        }
        val result = ai.result
        val action = result.action
        when (action) {
            "skill.help" -> helpSkill(event)
            "skill.status" -> statusSkill(event)
            "fallback.primary" -> event.message.addReaction("❓").queue()
            else -> event.message.channel.sendMessage(result.fulfillment.speech).queue()
        }
        log.info("Recieved \"${event.message.content}\" on shard ${event.jda.shardInfo.shardId}, acted with $action")
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.messageId in ledger) {
            event.channel.addReactionById(ledger[event.messageId], "❌").queue()
            event.channel.deleteMessageById(ledger[event.messageId]).queueAfter(1, TimeUnit.SECONDS)
            ledger.remove(event.messageId)
        }
    }
}