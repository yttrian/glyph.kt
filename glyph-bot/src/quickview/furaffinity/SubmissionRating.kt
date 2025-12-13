package org.yttr.glyph.bot.quickview.furaffinity

import java.awt.Color

/**
 * The submission rating (maturity level) of a Fur Affinity submission
 */
enum class SubmissionRating(
    /**
     * The color of the rating (based on the colors used on furaffinity.net)
     */
    val color: Color,
    /**
     * Whether or not the rating is considered a NSFW rating
     */
    val nsfw: Boolean
) {
    /**
     * Suitable for all-ages
     */
    General(Color.decode("#79A977"), false),

    /**
     * Gore, violence or tasteful/artistic nudity or mature themes.
     */
    Mature(Color.decode("#697CC1"), true),

    /**
     * Explicit or imagery otherwise geared towards adult audiences.
     */
    Adult(Color.decode("#992D22"), true)
}
