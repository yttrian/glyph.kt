/*
 * WikipediaExtractorTest.kt
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

package org.yttr.glyph.bot.skills.wiki

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull

internal class WikipediaExtractorTest {
    private val extractor = WikipediaExtractor()

    @Test
    fun `should get article via query`() = runBlocking {
        val massEffect = extractor.getArticle("Mass Effect")

        assertNotNull(massEffect)
        assert(massEffect.title == "Mass Effect")
        assert(massEffect.abstract.startsWith("Mass Effect is a military science fiction media franchise"))
    }

    @Test
    fun `should get thumbnail for article with thumbnail`() = runBlocking {
        val einstein = extractor.getArticle("Einstein")

        assert(einstein != null)

        if (einstein != null) {
            assert(einstein.title == "Albert Einstein")
            assert(einstein.thumbnail != null)
        }
    }

    @Test
    fun `should return null on non-existent result`() = runBlocking {
        val fake = extractor.getArticle("Real's not real")

        assert(fake == null)
    }
}
