package me.ianmooreis.glyph.skills

import ai.api.model.AIResponse
import com.github.kittinunf.fuel.Fuel
import me.ianmooreis.glyph.orchestrators.Skill
import me.ianmooreis.glyph.orchestrators.config
import me.ianmooreis.glyph.orchestrators.reply
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.json.JSONObject

object ServerConfigGetSkill : Skill("skill.configuration.view", serverOnly = true, requiredPermissions = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        Fuel.post("https://hastebin.com/documents").body(event.guild.config.toString()).response { request, response, result ->
            this.log.info(result.toString())
            event.message.reply(JSONObject(response.responseMessage).getString("key"))
        }
    }
}

object ServerConfigSetSkill : Skill("skill.configuration.load", serverOnly = true, requiredPermissions = listOf(Permission.ADMINISTRATOR)) {
    override fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        super.onTrigger(event, ai)
    }
}