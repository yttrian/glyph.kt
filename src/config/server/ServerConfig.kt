package org.yttr.glyph.config.server

import org.yttr.glyph.config.Config

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
