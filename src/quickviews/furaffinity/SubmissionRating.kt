package org.yttr.glyph.quickviews.furaffinity

import dev.kord.common.Color


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
    General(color = Color(rgb = 0x79A977), nsfw = false),

    /**
     * Gore, violence or tasteful/artistic nudity or mature themes.
     */
    Mature(color = Color(rgb = 0x697CC1), nsfw = true),

    /**
     * Explicit or imagery otherwise geared towards adult audiences.
     */
    Adult(color = Color(rgb = 0x992D22), nsfw = true)
}
