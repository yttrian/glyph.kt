package org.yttr.glyph.quickviews

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeoutOrNull
import org.yttr.glyph.quickviews.picarto.PicartoQuickView

object QuickViews {
    private const val TIMEOUT: Long = 10_000
    private const val LIMIT: Int = 5

    private val scanners = listOf(PicartoQuickView)

    suspend fun consume(event: MessageCreateEvent) {
        if (event.message.author?.isBot != false) {
            return
        }

        buildQuickViews(event.message).fold<_, Message?>(null) { message, quickview ->
            when (message) {
                null -> event.message
                    .edit { suppressEmbeds = true }
                    .reply { embeds = mutableListOf(quickview) }

                else -> message.edit { embeds?.plusAssign(quickview) }
            }
        }
    }

    private fun buildQuickViews(message: Message) = scanners.flatMap { scanner ->
        scanner.scan(message)
    }.asFlow().mapNotNull { quickview ->
        withTimeoutOrNull(TIMEOUT) {
            quickview.build()
        }
    }.take(LIMIT)
}
