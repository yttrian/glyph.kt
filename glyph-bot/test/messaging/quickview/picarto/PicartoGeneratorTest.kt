package org.yttr.glyph.bot.messaging.quickview.picarto

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

internal class PicartoGeneratorTest {
    private val generator = PicartoGenerator

    @ExperimentalCoroutinesApi
    @Test
    fun `should detect Picarto link in message`() = runBlocking {
        assert(generator.findChannelNames("a regular message without picarto").count() == 0)
        assert(generator.findChannelNames("https://picarto.tv/channel").first() == "channel")
    }
}
