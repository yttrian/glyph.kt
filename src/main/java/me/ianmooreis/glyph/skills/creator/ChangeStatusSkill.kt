package me.ianmooreis.glyph.skills.creator

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.StatusOrchestrator
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

/**
 * A skill that allows the creator to change the client status
 */
object ChangeStatusSkill : Skill("skill.creator.changeStatus", creatorOnly = true, cooldownTime = 30) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val jda = event.jda
        val name = ai.result.getStringParameter("status", jda.presence.game.name)
        val streamUrl = ai.result.getStringParameter("streamUrl", jda.presence.game.url)
        val gameType = ai.result.getStringParameter("gameType")
        val statusType = ai.result.getStringParameter("statusType")

        val game = when (gameType) {
            "playing" -> Game.playing(name)
            "listening" -> Game.listening(name)
            "watching" -> Game.watching(name)
            "streaming" -> Game.streaming(name, streamUrl)
            else -> jda.presence.game
        }
        val status = when (statusType) {
            "online" -> OnlineStatus.ONLINE
            "idle" -> OnlineStatus.IDLE
            "dnd" -> OnlineStatus.DO_NOT_DISTURB
            "invisible" -> OnlineStatus.INVISIBLE
            else -> jda.presence.status
        }

        StatusOrchestrator.setPresence(jda, status, game)

        event.message.reply(
            "Attempted to changed presence to ${status.name.toLowerCase()} " +
                "while ${game.type.toString().toLowerCase()} to ${game.name}! (May be rate limited)")
    }
}