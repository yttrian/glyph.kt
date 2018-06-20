package me.ianmooreis.glyph.configs

/**
 * A configuration for auditing
 */
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
     * The Discord webhook to send audits to
     */
    val webhook: String? = null)