package org.yttr.glyph.config.discord

import kotlinx.serialization.Serializable

/**
 * Represents a users relationship to a guild
 */
@Serializable
data class UserGuild(
    /**
     * The snowflake id of the guild
     */
    val id: String,
    /**
     * The name of the guild
     */
    val name: String,
    private val permissions: Int
) {
    /**
     * Whether or not the user has manage guild permission
     */
    val hasManageGuild: Boolean
        get() = permissions.and(PERMISSION_MANAGE_GUILD) == PERMISSION_MANAGE_GUILD

    companion object {
        private const val PERMISSION_MANAGE_GUILD: Int = 0x00000020
    }
}
