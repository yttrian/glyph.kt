package me.ianmooreis.glyph.configs

/**
 * A configuration for wikis
 */
data class WikiConfig(
    /**
     * The list of wiki sources to search in order
     */
    val sources: List<String?> = listOf("wikipedia", "masseffect", "avp"),
    /**
     * The minimum Wikia article quality to allow pass
     */
    val minimumQuality: Int = 50)