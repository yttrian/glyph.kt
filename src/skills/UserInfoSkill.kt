package org.yttr.glyph.skills

import dev.kord.core.event.message.MessageCreateEvent
import net.dv8tion.jda.api.entities.User
import org.yttr.glyph.ai.AIResponse
import org.yttr.glyph.extensions.cleanMentionedMembers
import org.yttr.glyph.extensions.getInfoEmbed
import org.yttr.glyph.messaging.Response

/**
 * A skill that allows users to get an info embed about other or themselves
 */
class UserInfoSkill : Skill("skill.moderation.userInfo") {
    override suspend fun perform(event: MessageCreateEvent, ai: AIResponse) {
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
