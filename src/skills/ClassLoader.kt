package org.yttr.glyph.skills

/**
 * Read a Markdown file from the resources, treating single linebreaks as continuations
 */
@Deprecated("Consider better ways of reading string resources!", ReplaceWith(""))
fun ClassLoader.readMarkdown(resourceFileName: String): String? =
    this.getResourceAsStream(resourceFileName)?.bufferedReader()?.readLines()?.joinToString("") {
        if (it.isBlank()) "\n\n" else (it.trim() + " ")
    }
