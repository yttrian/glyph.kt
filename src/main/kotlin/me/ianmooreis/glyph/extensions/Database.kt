/*
 * Database.kt
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

package me.ianmooreis.glyph.extensions

import java.sql.PreparedStatement
import java.sql.ResultSet

//TODO: Something better than this
/**
 * Attempts to convert an array from a PostgreSQL database into a list
 *
 * @param columnLabel the name of the column with the array
 *
 * @return hopefully a list from the array string
 */
fun ResultSet.getList(columnLabel: String): List<String> { //This is probably the stupidest thing in the history of stupid things, maybe ever.
    return this.getArray(columnLabel).toString()
        .removeSurrounding("{", "}")
        .split(",")
        .map { it.removeSurrounding("\"") }
}

/**
 * Attempts to add a list as a value in a prepared statement
 *
 * @param parameterIndex the index of the prepared parameter
 * @param list the list to set as a text array in the prepared statement
 */
fun PreparedStatement.setList(parameterIndex: Int, list: List<String?>) {
    this.setArray(parameterIndex, this.connection.createArrayOf("text", list.filterNotNull().filter { it != "" }.toTypedArray()))
}