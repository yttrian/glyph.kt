package org.yttr.glyph.bot.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

/**
 * Shorthand for building an embed
 */
fun embedBuilder(builder: EmbedBuilder.() -> Unit): MessageEmbed =
    EmbedBuilder().also(builder).build()
