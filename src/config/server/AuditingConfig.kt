package org.yttr.glyph.config.server

import org.yttr.glyph.config.Config

/**
 * A configuration for auditing
 */
@Serializable
data class AuditingConfig(
    /**
     * Whether or not to audit member joins
     */
    val joins: Boolean = false,
    /**
     * Whether or not to audit member leaves
     */
    val leaves: Boolean = false,
    /**
     * Whether or not to purges
     */
    val purge: Boolean = false,
    /**
     * Whether or not to audit kicks
     */
    val kicks: Boolean = false,
    /**
     * Whether or not to audit bans
     */
    val bans: Boolean = false,
    /**
     * Whether or not to audit username changes
     */
    val names: Boolean = false,
    /**
     * The channel to send audits to
     */
    val channel: Long? = null
) : Config
