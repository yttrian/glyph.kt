package org.yttr.glyph.skills.wiki

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
