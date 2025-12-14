package org.yttr.glyph.bot

import java.net.URL

object Resources {
    fun readText(path: String): String = getResource(path).readText()

    private fun getResource(path: String): URL {
        val absolutePath = "/" + path.trimStart('/')
        val resource = Resources::class.java.getResource(absolutePath)

        requireNotNull(resource) { "Resource $absolutePath not found!" }

        return resource
    }
}
