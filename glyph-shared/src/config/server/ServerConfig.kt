package org.yttr.glyph.shared.config.server

import kotlinx.serialization.Serializable
import org.yttr.glyph.shared.config.Config

/**
 * The holder of all the sub-configurations
 */
@Serializable
data class ServerConfig(
    /**
     * The wiki config
     */
    val wiki: WikiConfig = WikiConfig(),
    /**
     * The selectable roles config
     */
    val selectableRoles: SelectableRolesConfig = SelectableRolesConfig(),
    /**
     * The QuickView config
     */
    val quickview: QuickviewConfig = QuickviewConfig(),
    /**
     * The auditing config
     */
    val auditing: AuditingConfig = AuditingConfig(),
    /**
     * The starboard config
     */
    val starboard: StarboardConfig = StarboardConfig()
) : Config
