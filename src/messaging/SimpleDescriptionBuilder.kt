package org.yttr.glyph.directors.messaging

/**
 * A simple way to present a list of information with a one-world field name in bold and a description
 */
class SimpleDescriptionBuilder(
    /**
     * Whether or not formatting like italics or bold should be used and/or allowed when built
     */
    private val noFormatting: Boolean = false
) {
    private var fields: MutableList<Pair<String?, String>> = mutableListOf()

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be max 1 word
     * @param content the field content
     */
    fun addField(name: String?, content: String): SimpleDescriptionBuilder {
        if (name != null && name.split(" ").size != 1) {
            throw IllegalArgumentException("The field name must be max 1 word.")
        }
        fields.add(Pair(name, content))
        return this
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String?, content: Int): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String?, content: Long): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Converts a simple description to a usable string
     */
    fun build(): String {
        return this.toString()
    }

    override fun toString(): String {
        return fields.joinToString("\n") { (name, value) ->
            when {
                name == null -> value
                noFormatting -> "$name $value".replace("*", "")
                else -> "**$name** $value"
            }
        }
    }
}
