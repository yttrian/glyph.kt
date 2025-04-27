package org.yttr.glyph.shared.config.server

import kotlinx.serialization.Serializable
import org.yttr.glyph.shared.config.Config

/**
 * A configuration for QuickViews
 */
@Serializable
data class QuickviewConfig(
    /**
     * Whether or not FurAffinity QuickViews are enabled
     */
    val furaffinityEnabled: Boolean = true,
    /**
     * Whether or not FurAffinity QuickViews should show thumbnails
     */
    val furaffinityThumbnails: Boolean = true,
    /**
     * Whether or not Picarto QuickViews are enabled
     */
    val picartoEnabled: Boolean = true
) : Config
