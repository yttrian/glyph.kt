package me.ianmooreis.glyph.orchestrators.messaging

/**
 * A simple way to present a list of information with a one-world field name in bold and a description
 */
class SimpleDescriptionBuilder(
    /**
     * Whether or not formatting like italics or bold should be used and/or allowed when built
     */
    private val noFormatting: Boolean = false) {
    private var fields: MutableMap<String, String> = mutableMapOf()

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String, content: String): SimpleDescriptionBuilder {
        if (name.split(" ").size != 1) {
            throw IllegalArgumentException("The field name must be exactly 1 word. No more, no less!")
        }
        fields[name] = content
        return this
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String, content: Int): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Add a field to a simple description
     *
     * @param name the field name, must be 1 word only
     * @param content the field content
     */
    fun addField(name: String, content: Long): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    /**
     * Converts a simple description to a usable string
     */
    fun build(): String {
        return this.toString()
    }

    override fun toString(): String {
        return fields.map {
            if (noFormatting) {
                "${it.key} ${it.value}".replace("*", "")
            } else {
                "**${it.key}** ${it.value}"
            }
        }.joinToString("\n")
    }
}