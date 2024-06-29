package org.yttr.glyph.quickviews

import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeoutOrNull
import org.yttr.glyph.Director
import org.yttr.glyph.quickviews.furaffinity.FurAffinityQuickView
import org.yttr.glyph.quickviews.picarto.PicartoQuickView

object QuickViewDirector : Director {
    private const val TIMEOUT: Long = 10_000
    private const val LIMIT: Int = 5

    private val scanners = listOf(FurAffinityQuickView, PicartoQuickView)

    override fun register(kord: Kord) {
        kord.on<MessageCreateEvent>(consumer = ::consume)
    }

    private suspend fun consume(event: MessageCreateEvent) {
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
