/*
 * FurAffinityGeneratorTest.kt
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

package messaging.quickview.furaffinity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.ianmooreis.glyph.messaging.quickview.furaffinity.FurAffinityGenerator
import me.ianmooreis.glyph.messaging.quickview.furaffinity.SubmissionRating
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for the FurAffinity QuickView generator
 *
 * All tests use the "Fender" mascot account
 */
internal class FurAffinityGeneratorTest {
    private val generator = FurAffinityGenerator()

    @ExperimentalCoroutinesApi
    @Test
    fun findSubmissionId() = runBlocking {
        // https://www.furaffinity.net/view/4483888/
        val submissionId = generator.findSubmissionId(1284661300, "Fender")

        assert(submissionId == 4483888)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun findIds() = runBlocking {
        // a regular message
        assert(generator.findIds("nothing of substance example.net/view/12345").count() == 0)
        // a message with a submission url
        assert(generator.findIds("https://www.furaffinity.net/view/12239491/").first() == 12239491)
        // there should be no duplicates
        assert(
            generator.findIds("https://www.furaffinity.net/view/12239491/ http://www.furaffinity.net/view/9719932/ furaffinity.net/view/12239491/")
                .count() == 2
        )
    }

    @Test
    fun findIdsOnline() = runBlocking {
        // a message with a CDN url https://www.furaffinity.net/view/10796676/
        assert(
            generator.findIds("https://d.facdn.net/art/fender/1370725527/1370725527.fender_kyma_fau.jpg")
                .first() == 10796676
        )
    }

    @Test
    fun getSubmission() = runBlocking {
        // https://www.furaffinity.net/view/9719932/
        val submission = generator.getSubmission(9719932)

        assertNotNull(submission)

        assert(submission?.title == "Applied Sciences Units 01 and 02")
        assert(submission?.name == "Fender")
        assert(submission?.download == "https://d.facdn.net/art/fender/1358541618/1358541618.fender_kiryu_promoart.jpg")
        assert(submission?.keywords?.containsAll(listOf("mecha", "robots", "rednef")) ?: false)
        assert(submission?.gender == "Other / Not Specified")
        assert(submission?.resolution == "900x643")
        assert(submission?.rating == SubmissionRating.General)
    }
}