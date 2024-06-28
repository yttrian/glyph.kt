package org.yttr.glyph.quickviews.furaffinity

import kotlinx.serialization.Serializable

/**
 * Represents a user page in the API
 */
@Serializable
data class UserPage(
    /**
     * Total number of submissions the user has
     */
    val submissions: Int
)
