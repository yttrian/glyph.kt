package me.ianmooreis.glyph.skills.wiki

import java.net.URL

/**
 * A wiki article
 */
data class WikiArticle(
    /**
     * The title of the article
     */
    val title: String,
    /**
     * The short intro text of the article
     */
    val intro: String,
    /**
     * The url linking to the article
     */
    val url: URL)