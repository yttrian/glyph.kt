package me.ianmooreis.glyph.orchestrators.messaging

class SimpleDescriptionBuilder {
    private var fields: MutableMap<String, String> = mutableMapOf()

    fun addField(name: String, content: String): SimpleDescriptionBuilder {
        if (name.split(" ").size != 1) {
            throw IllegalArgumentException("The field name must be exactly 1 word. No more, no less!")
        }
        fields[name] = content
        return this
    }

    fun addField(name: String, content: Int): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    fun addField(name: String, content: Long): SimpleDescriptionBuilder {
        return addField(name, content.toString())
    }

    fun build(): String {
        return this.toString()
    }

    override fun toString(): String {
        return fields.map { "**${it.key}** ${it.value}" }.joinToString("\n")
    }
}