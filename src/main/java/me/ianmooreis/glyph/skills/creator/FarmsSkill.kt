package me.ianmooreis.glyph.skills.creator

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.botRatio
import me.ianmooreis.glyph.extensions.isBotFarm
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.SkillAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object FarmsSkill : SkillAdapter("skill.creator.farms", creatorOnly = true) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        val action = ai.result.getStringParameter("action", "list")
        val farms = event.jda.guilds.filter { it.isBotFarm }
        val farmsText = if (farms.isNotEmpty()) farms.joinToString("\n") { "${it.name} (${it.botRatio})" } else "No farms."
        event.channel.sendTyping().queue()
        when (action)  {
            "list" -> event.message.reply("**Farms List**\n$farmsText")
            "leave" -> {
                event.message.reply("**Farms Left**\n$farmsText")
                farms.forEach { guild ->
                    guild.leave().queue {
                        log.info("Left bot farm $guild! Guild had bot ratio of ${guild.botRatio}")
                    }
                }
            }
        }
    }
}