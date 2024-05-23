package org.yttr.glyph

/**
 * Read a Markdown file from the resources, treating single linebreaks as continuations
 */
fun ClassLoader.readMarkdown(resourceFileName: String): String? =
    this.getResourceAsStream(resourceFileName)?.bufferedReader()?.readLines()?.joinToString("") {
        if (it.isBlank()) "\n\n" else (it.trim() + " ")
    }
