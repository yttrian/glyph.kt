package me.ianmooreis.glyph.orchestrators.messaging.quickview.furaffinity

import java.awt.Color

/**
 * The submission rating (maturity level) of a FurAffinity submission
 */
enum class SubmissionRating(
    /**
     * The color of the rating (based on the colors used on furaffinity.net)
     */
    val color: Color,
    /**
     * Whether or not the rating is considered a NSFW rating
     */
    val nsfw: Boolean) {

    /**
     * Suitable for all-ages
     */
    General(Color.GREEN, false),
    /**
     * Gore, violence or tasteful/artistic nudity or mature themes.
     */
    Mature(Color.BLUE, true),
    /**
     * Explicit or imagery otherwise geared towards adult audiences.
     */
    Adult(Color.RED, true)
}