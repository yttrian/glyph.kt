package me.ianmooreis.glyph.utils.quickview.furaffinity

import java.awt.Color

enum class SubmissionRating(val color: Color, val nsfw: Boolean) {
    General(Color.GREEN, false),
    Mature(Color.BLUE, true),
    Adult(Color.RED, true)
}