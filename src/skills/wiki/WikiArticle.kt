package org.yttr.glyph.skills.wiki

/**
 * A wiki article
 */
data class WikiArticle(
    /**
     * The title of the article
     */
    val title: String,
    /**
     * The short text of the article
     */
    val abstract: String,
    /**
     * The url linking to the article
     */
    val url: String,
    /**
     * The url of a thumbnail, if any
     */
    val thumbnail: String? = null
)
