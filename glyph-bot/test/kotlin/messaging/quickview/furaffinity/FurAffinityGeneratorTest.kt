package org.yttr.glyph.bot.messaging.quickview.furaffinity

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for the FurAffinity QuickView generator
 *
 * All tests use the "Fender" mascot account
 */
@Suppress("HttpUrlsUsage")
internal class FurAffinityGeneratorTest {
    private val generator = FurAffinityGenerator()

    @Test
    fun `should find submission id from CDN id`() = runBlocking {
        // https://www.furaffinity.net/view/4483888/
        val submissionId = generator.findSubmissionId(1284661300, "Fender")

        assert(submissionId == 4483888)
    }

    @Test
    fun `should find distinct ids in a message`() = runBlocking {
        // a regular message
        assertEquals(0, generator.findIds("nothing of substance example.net/view/12345").count())
        // a message with a submission url
        assertEquals(12239491, generator.findIds("https://www.furaffinity.net/view/12239491/").first())
        // there should be no duplicates
        assertEquals(
            2,
            generator.findIds(
                "https://www.furaffinity.net/view/12239491/ " +
                        "http://www.furaffinity.net/view/9719932/ " +
                        "furaffinity.net/view/12239491/"
            ).count()
        )
    }

    @Test
    fun `should find submission id from CDN url`() = runBlocking {
        // a message with a CDN url https://www.furaffinity.net/view/10796676/
        assert(
            generator.findIds("https://d.facdn.net/art/fender/1370725527/1370725527.fender_kyma_fau.jpg")
                .first() == 10796676
        )
    }

    @Test
    fun `should properly retrieve Applied Sciences Units 01 and 02`() = runBlocking {
        // https://www.furaffinity.net/view/9719932/
        val submission = generator.getSubmission(9719932)

        assertNotNull(submission)

        assertEquals("Applied Sciences Units 01 and 02", submission.title)
        assertEquals("Fender", submission.name)
        assertEquals(
            "https://d.furaffinity.net/art/fender/1358541618/1358541618.fender_kiryu_promoart.jpg",
            submission.download
        )
        assert(submission.keywords.containsAll(listOf("mecha", "robots", "rednef")))
        assertEquals("Other / Not Specified", submission.gender)
        assertEquals("900x643", submission.resolution)
        assertEquals(SubmissionRating.General, submission.rating)
    }

    @Test
    fun `should return null on invalid submission id`() = runBlocking {
        assertNull(generator.getSubmission(0))
    }
}
