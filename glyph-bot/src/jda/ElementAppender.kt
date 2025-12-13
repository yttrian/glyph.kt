package org.yttr.glyph.bot.jda

fun interface ElementAppender<T> {
    operator fun plusAssign(element: T)
}
