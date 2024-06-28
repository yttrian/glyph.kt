package org.yttr.glyph.quickviews.furaffinity

import kotlinx.serialization.Serializable

/**
 * Represents a submission excerpt from the submission listing endpoint of the API
 */
@Serializable
data class SubmissionExcerpt(
    /**
     * Submission id
     */
    val id: Int,
    /**
     * Submission thumbnail URL
     */
    val thumbnail: String
)
