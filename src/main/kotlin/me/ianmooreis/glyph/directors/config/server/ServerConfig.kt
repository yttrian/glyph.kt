/*
 * ServerConfig.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2019 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ianmooreis.glyph.directors.config.server

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.ianmooreis.glyph.directors.config.ConfigContainer
import net.dv8tion.jda.core.entities.Guild

/**
 * The holder of all the sub-configurations
 */
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
     * The auto moderator config
     */
    val crucible: AutoModConfig = AutoModConfig(),
    /**
     * The starboard config
     */
    val starboard: StarboardConfig = StarboardConfig()
) : ConfigContainer {
    override fun toJSON(guild: Guild): String {
        val serializer = GsonBuilder().serializeNulls().create()
        val config = serializer.toJsonTree(this).asJsonObject
        val serverDetails = JsonObject()

        // Let's add some extra useful details about the server for suggestions
        val roles = JsonArray()
        guild.roles.forEach {
            val roleData = JsonObject()
            roleData.addProperty("id", it.idLong)
            roleData.addProperty("name", it.name)
            roleData.addProperty("position", it.position)
            roles.add(roleData)
        }
        serverDetails.add("roles", roles)

        // Package it all up and send it out
        config.add("_details", serverDetails)
        return config.toString()
    }

    override fun fromJSON(json: String): ServerConfig = Gson().fromJson(json, ServerConfig::class.java)
}