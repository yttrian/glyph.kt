package org.yttr.glyph.bot

import java.net.URL

object Resources {
    /**
     * Read a text resource for the root
     */
    fun readText(path: String): String = getResource(path).readText()

    /**
     * Read a Markdown resource for the root, with basic support for wrapped lines
     */
    fun readMarkdown(path: String): String = readText(path)
        .lines()
        .joinToString(" ") { line ->
            if (line.isEmpty()) "\n\n" else line.trim()
        }

    private fun getResource(path: String): URL {
        val absolutePath = "/" + path.trimStart('/')
        val resource = Resources::class.java.getResource(absolutePath)

        requireNotNull(resource) { "Resource $absolutePath not found!" }

        return resource
    }
}
