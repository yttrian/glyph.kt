package org.yttr.glyph.quickviews

import dev.kord.common.entity.optional.value
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder

interface QuickView {
    /**
     * Build the QuickView if possible
     */
    suspend fun build(): EmbedBuilder?

    interface Scanner<T : QuickView> {
        /**
         * Scan a message and report possible QuickViews
         */
        fun scan(message: Message): List<T>
    }
}

/**
 * Is NSFW allowed in the message's channel?
 */
suspend fun Message.isNsfwAllowed(): Boolean = getChannelOrNull()?.data?.nsfw?.value == true
