package org.yttr.glyph.config.server

import kotlinx.serialization.Serializable
import org.yttr.glyph.config.Config

/**
 * A configuration for selectable roles
 */
@Serializable
data class SelectableRolesConfig(
    /**
     * The list of selectable roles
     */
    val roles: List<Long> = emptyList(),
    /**
     * How many selectable roles a member can have at once
     */
    val limit: Int = 1
) : Config
