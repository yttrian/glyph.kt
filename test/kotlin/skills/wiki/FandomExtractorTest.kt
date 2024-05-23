package org.yttr.glyph.skills.wiki

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

internal class FandomExtractorTest {
    private val extractor = FandomExtractor("masseffect", 0)

    @Test
    fun `should properly find Garrus on masseffect wiki`() = runBlocking {
        val garrus = extractor.getArticle("Garrus")

        assert(garrus != null)

        if (garrus != null) {
            assert(garrus.title == "Garrus Vakarian")
            assert(garrus.abstract.startsWith("Garrus Vakarian is a turian"))
            assert(garrus.thumbnail != null)
        }
    }

    @Test
    fun `should return null on non-existent result`() = runBlocking {
        val fake = extractor.getArticle("realsnotreal")

        assert(fake == null)
    }
}
