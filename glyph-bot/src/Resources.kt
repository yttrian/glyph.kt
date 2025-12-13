package org.yttr.glyph.bot

object Resources {
    fun readText(path: String): String {
        val resource = Resources::class.java.getResource(path)

        requireNotNull(resource) { "Resource $path not found!" }

        return resource.readText()
    }
}
