package me.ianmooreis.glyph.configs

/**
 * A configuration for selectable roles
 */
data class SelectableRolesConfig(
    /**
     * The list of selectable roles
     */
    val roles: List<String?> = emptyList(),
    /**
     * How many selectable roles a member can have at once
     */
    val limit: Int = 1)