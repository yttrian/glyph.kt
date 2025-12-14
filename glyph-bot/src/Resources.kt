package org.yttr.glyph.bot

import java.net.URL

object Resources {
    fun readText(path: String): String = getResource(path).readText()

    private fun getResource(path: String): URL =
        requireNotNull(value = Resources::class.java.getResource(path)) { "Resource $path not found!" }
}
