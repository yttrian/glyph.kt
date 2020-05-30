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
import java.util.zip.Inflater

/**
 * An array based, message-packed, base64 url encoded serialization that requires knowledge of the layout on both ends
 */
sealed class MicroConfig {
    /**
     * Build a MicroConfig
     */
    class Builder : MicroConfig() {
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

    /**
     * Read a MicroConfig
     */
    class Reader : MicroConfig() {
        private val config = mutableListOf<List<String?>>()

        /**
         * Pull a string from a known section and index
         */
        fun pullString(section: Int, index: Int): String? = config[section][index]

        /**
         * Pull a boolean combination from a known section and index
         */
        fun pullBooleans(section: Int, index: Int, count: Int): List<Boolean> {
            val booleanCombo = pullInt(section, index)
            return (0..count).map { booleanCombo?.and(1.shl(it)) == 1 }
        }

        /**
         * Pull a long from a known section and index
         */
        fun pullLong(section: Int, index: Int): Long? = config[section][index]?.toLong(RADIX)

        /**
         * Pull an integer from a known section and index
         */
        fun pullInt(section: Int, index: Int): Int? = config[section][index]?.toInt(RADIX)

        /**
         * Pull a list of strings from a known section
         */
        fun pullStringList(section: Int, startIndex: Int): List<String> =
            config[section].listIterator(startIndex).asSequence().toList().filterNotNull()

        /**
         * Pull a list of longs from a known section
         */
        fun pullLongList(section: Int, startIndex: Int): List<Long> =
            config[section].listIterator(startIndex).asSequence().toList().mapNotNull { it?.toLong(RADIX) }

        /**
         * Read in a MicroConfig from the string representation
         */
        @Throws(IllegalArgumentException::class)
        suspend fun read(configString: String): Reader {
            val data = withContext(Dispatchers.IO) {
                val decompressor = Inflater()
                val zstring = Base64.decodeBase64(configString)
                decompressor.setInput(zstring)
                val out = ByteBuffer.allocate(zstring.size)
                decompressor.inflate(out)
                decompressor.end()
                val unpack: List<Any?> = MoshiPack().unpack(out.array())
                unpack
            }

            var section = mutableListOf<String?>()
            data.forEach {
                when (it) {
                    is Number -> {
                        if (section.isNotEmpty()) {
                            config.add(section)
                            section = mutableListOf()
                        }
                    }
                    is String? -> {
                        section.add(it)
                    }
                }
            }

            return this
        }
    }

    companion object {
        /**
         * Base used for numerical values
         */
        const val RADIX: Int = 36
    }
}
