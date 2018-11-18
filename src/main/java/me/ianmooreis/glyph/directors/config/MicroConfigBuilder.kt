/*
 * MicroConfigBuilder.kt
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

package me.ianmooreis.glyph.directors.config

/**
 * A builder to assist in the creation of a micro-config
 */
class MicroConfigBuilder {
    /**
     * The list of values in the micro-config
     */
    private val values = mutableListOf<String>()

    private fun addValue(value: String) {
        values.add(value)
    }

    /**
     * Add a string to the micro-config
     */
    fun addValue(vararg newValues: String?): MicroConfigBuilder {
        newValues.forEach {
            val corrected = it?.removePrefix("https://discordapp.com/api/webhooks/") ?: "!"
            addValue(corrected)
        }
        return this
    }

    /**
     * Add an integer value to the micro-config
     */
    fun addValue(vararg newValues: Int): MicroConfigBuilder {
        newValues.forEach {
            addValue(it.toString())
        }
        return this
    }

    /**
     * Add a boolean value to the micro-config
     */
    fun addValue(vararg newValues: Boolean): MicroConfigBuilder {
        newValues.forEach {
            addValue(it.toInt().toString())
        }
        return this
    }

    /**
     * Build a micro-config off of all the values that have been provided
     */
    fun build(): MicroConfig {
        return MicroConfig(values)
    }

    /**
     * Convert a boolean to an integer
     */
    private fun Boolean.toInt(): Int {
        return if (this) 1 else 0
    }
}