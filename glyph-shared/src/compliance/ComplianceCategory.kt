package org.yttr.glyph.shared.compliance

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import org.yttr.glyph.shared.readMarkdown

/**
 * Categories of data usage that require compliance tracking
 */
enum class ComplianceCategory(
    /**
     * The default assumption to make when missing data.
     */
    val optInDefault: Boolean
) {
    /**
     * Google Cloud Dialogflow
     */
    Dialogflow(false),

    /**
     * Message starboard skill
     */
    Starboard(true),

    /**
     * Embed QuickViews skill
     */
    QuickView(true);

    /**
     * Message to show the user when requesting a compliance decision from them.
     */
    fun buildComplianceMessage(optedIn: Boolean? = null): Message {
        val name = this.name

        return MessageBuilder().apply {
            val embed = EmbedBuilder().apply {
                setTitle("$name Compliance")
                setDescription(this::class.java.classLoader.readMarkdown("compliance/$name.md"))

                if (optedIn != null) {
                    setFooter("You are currently " + if (optedIn) "opted in" else "opted out")
                }
            }.build()

            setEmbeds(embed)
            setActionRows(
                ActionRow.of(
                    Button.success("Compliance:$name:In", "Opt in to $name"),
                    Button.danger("Compliance:$name:Out", "Opt out of $name"),
                    Button.link("https://glyph.yttr.org/privacy", "Read Privacy Policy")
                )
            )
        }.build()
    }
}
