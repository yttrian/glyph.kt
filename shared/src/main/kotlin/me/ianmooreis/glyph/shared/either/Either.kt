/*
 * Either.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package me.ianmooreis.glyph.shared.either

/**
 * Either type, Left or Right
 */
sealed class Either<out L, out R> {
    /**
     * Left of an Either
     */
    data class Left<out L>(
        /**
         * Value of the Left
         */
        private val v: L
    ) : Either<L, Nothing>() {
        /**
         * Pull value for Left
         */
        operator fun invoke(): L = v
    }

    /**
     * Right of an Either
     */
    data class Right<out R>(
        /**
         * Value of the Right
         */
        private val v: R
    ) : Either<Nothing, R>() {
        /**
         * Pull value from Right
         */
        operator fun invoke(): R = v
    }
}

/**
 * Wrap value in Left
 */
fun <T> T.left(): Either.Left<T> = Either.Left(this)

/**
 * Wrap value in Right
 */
fun <T> T.right(): Either.Right<T> = Either.Right(this)
