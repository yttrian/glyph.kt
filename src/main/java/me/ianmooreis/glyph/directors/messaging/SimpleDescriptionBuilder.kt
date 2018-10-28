/*
 * SimpleDescriptionBuilder.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors.messaging

/**
 * A simple way to present a list of information with a one-world field name in bold and a description
 */
class SimpleDescriptionBuilder(
    /**
     * Whether or not formatting like italics or bold should be used and/or allowed when built
     */
    private val noFormatting: Boolean = false) {
    private var fields: MutableList<Pair<String?, String>> = mutableListOf()

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be max 1 word
     * @param content the field content
     */
    fun addField(name: String?, content: String): SimpleDescriptionBuilder {
        if (name != null && name.split(" ").size != 1) {
            throw IllegalArgumentException("The field name must be max 1 word.")
        }
        fields.add(Pair(name, content))
        return this
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String?, content: Int): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String?, content: Long): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Converts a simple description to a usable string
     */
    fun build(): String {
        return this.toString()
    }

    override fun toString(): String {
        return fields.joinToString("\n") { (name, value) ->
            when {
                name == null -> value
                noFormatting -> "$name $value".replace("*", "")
                else -> "**$name** $value"
            }
        }
    }
}