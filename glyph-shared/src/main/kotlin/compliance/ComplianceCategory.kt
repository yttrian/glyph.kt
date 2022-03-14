/*
 * ComplianceCategory.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2022 by Ian Moore
 *
 * This file is part of Glyph.
 *
 * Glyph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
                    Button.link("https://gl.yttr.org/privacy", "Read Privacy Policy")
                )
            )
        }.build()
    }
}
