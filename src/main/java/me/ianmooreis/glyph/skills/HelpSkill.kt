package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import me.ianmooreis.glyph.extensions.reply
import me.ianmooreis.glyph.orchestrators.skills.Skill
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

/**
 * A skill that shows users a help messgae
 */
object HelpSkill : Skill("skill.help") {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        //val creator: User = event.jda.getUserById(System.getenv("CREATOR_ID"))
        val name = event.jda.selfUser.name
        val embed = EmbedBuilder()
            .setTitle("$name Help")
            .setDescription(ai.result.fulfillment.speech.replace("\\n", "\n", true))
            .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
            .build()
        event.message.reply(embed = embed)
    }
}