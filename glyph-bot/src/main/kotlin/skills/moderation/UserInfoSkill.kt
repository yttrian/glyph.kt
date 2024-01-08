package org.yttr.glyph.bot.skills.moderation

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.extensions.cleanMentionedMembers
import org.yttr.glyph.bot.extensions.getInfoEmbed
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.bot.skills.Skill

/**
 * A skill that allows users to get an info embed about other or themselves
 */
class UserInfoSkill : Skill("skill.moderation.userInfo") {
    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        // val userName: String? = ai.result.getStringParameter("user")
        val user: User = event.message.cleanMentionedMembers.firstOrNull()?.user ?: event.author
        return Response.Volatile(
            user.getInfoEmbed(
                "User Info",
                "Moderation",
                null,
                showExactCreationDate = true,
                mutualGuilds = false
            )
        )
    }
}
