package me.ianmooreis.glyph.configs

/**
 * A configuration for QuickViews
 */
data class QuickviewConfig(
    /**
     * Whether or not FurAffinity QuickViews are enabled
     */
    val furaffinityEnabled: Boolean = true,
    /**
     * Whether or not FurAffinity QuickViews should show thumbnails
     */
    val furaffinityThumbnails: Boolean = false,
    /**
     * Whether or not Picarto QuickViews are enabled
     */
    val picartoEnabled: Boolean = true)