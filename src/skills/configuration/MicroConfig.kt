/*
 * MicroConfig.kt
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

package me.ianmooreis.glyph.skills.configuration

import com.daveanthonythomas.moshipack.MoshiPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base64
import java.nio.ByteBuffer
import java.util.zip.Deflater

/**
 * An array based, message-packed, base64 url encoded serialization that requires knowledge of the layout on both ends
 */
class MicroConfig {
    /**
     * Build a MicroConfig
     */
    class Builder {
        private val config = mutableListOf<Any?>()
        private var lastSection = 0

        /**
         * Add a string element
         */
        fun push(string: String): Boolean = config.add(string)

        /**
         * Create a boolean combination element
         */
        fun push(vararg boolean: Boolean): Boolean {
            var booleanCombo = 0
            boolean.forEachIndexed { index, b ->
                booleanCombo = booleanCombo.or((if (b) 1 else 0).shl(index))
            }
            return push(booleanCombo)
        }

        /**
         * Add a base-36 converted long (typically for snowflake IDs)
         */
        fun push(long: Long?): Boolean = config.add(long?.toString(RADIX))

        /**
         * Add a base-36 converted integer
         */
        fun push(int: Int): Boolean = config.add(int.toString(RADIX))

        /**
         * Start a new section
         */
        fun startSection() {
            config.add(lastSection, config.size - lastSection)
            lastSection = config.size
        }

        /**
         * Build the output string
         */
        suspend fun build(): String = withContext(Dispatchers.IO) {
            startSection()
            val pack = MoshiPack().pack(config).readByteArray()
            val compressor = Deflater()
            compressor.setInput(pack)
            compressor.finish()
            val out = ByteBuffer.allocate(pack.size)
            compressor.deflate(out)
            compressor.end()
            Base64.encodeBase64URLSafeString(out.array())
        }
    }

    companion object {
        /**
         * Base used for numerical values
         */
        const val RADIX: Int = 36
    }
}
