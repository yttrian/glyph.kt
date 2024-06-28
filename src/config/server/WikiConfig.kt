package org.yttr.glyph.config.server

import kotlinx.serialization.Serializable
import org.yttr.glyph.config.Config

/**
 * A configuration for wikis
 */
@Serializable
data class WikiConfig(
    /**
     * The list of wiki sources to search in order
     */
    val sources: List<String> = listOf(),
    /**
     * The minimum Wikia article quality to allow pass
     */
    val minimumQuality: Int = 50
) : Config
