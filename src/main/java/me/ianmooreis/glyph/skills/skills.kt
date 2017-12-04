package me.ianmooreis.glyph.skills

import club.minnced.kjda.promise
import me.ianmooreis.glyph.MessageOrchestrator
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

object Skills {
    fun help(event: MessageReceivedEvent) {
        val embed = EmbedBuilder()
                .setTitle("Glyph Help")
                .setDescription("I am **Glyph**!\n\n" +
                        "A **constantly evolving** and **learning** Discord bot created by Throudin#4867.\n\n" +
                        "I use **machine learning** to process **natural language** requests you give to me to the best of my current trained ability.\n\n" +
                        "To see what I can do, be sure to check out my **full skills list** and **suggest new ones** you'd like to see, in the official server.\n\n" +
                        "[Full Skills List](https://glyph-discord.readthedocs.io/en/latest/skills.html) - " +
                        "[Official Glyph Server](https://discord.me/glyph-discord) - " +
                        "[Add Me To Your Server](http://glyph-discord.rtfd.io/invite)")
                .setColor(Color.getHSBColor(0.6f, 0.89f, 0.61f))
                .build()
        event.message.channel.sendMessage(embed).promise().then {
            MessageOrchestrator.ammendLedger(event.messageId, it.id)
        }
    }
}