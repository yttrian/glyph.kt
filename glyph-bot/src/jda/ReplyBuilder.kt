package org.yttr.glyph.bot.jda

import net.dv8tion.jda.api.components.MessageTopLevelComponent
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

class ReplyBuilder(private val action: ReplyCallbackAction) {
    val embeds = ElementAppender<MessageEmbed> { action.addEmbeds(it) }
    val components = ElementAppender<MessageTopLevelComponent> { action.addComponents(it) }

    var content: String?
        get() = action.content
        set(value) {
            action.setContent(value)
        }

    var ephemeral: Boolean = false
        set(value) {
            action.setEphemeral(value)
            field = value
        }
}


fun IReplyCallback.buildReply(content: String = "", block: ReplyBuilder.() -> Unit): ReplyCallbackAction {
    val action = reply(content)
    ReplyBuilder(action).block()
    return action
}
