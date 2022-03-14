/*
 * ComplianceListener.kt
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

package org.yttr.glyph.bot.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.LoggerFactory
import org.yttr.glyph.shared.compliance.ComplianceCategory
import org.yttr.glyph.shared.compliance.ComplianceOfficer

/**
 * Compliance management listener
 */
object ComplianceListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(javaClass.simpleName)
    private val scope = CoroutineScope(SupervisorJob())

    private const val COMMAND_NAME = "compliance"
    private const val OPTION_NAME = "category"

    /**
     * Upsert the command for managing compliance categories.
     */
    override fun onReady(event: ReadyEvent) {
        val option = OptionData(OptionType.STRING, OPTION_NAME, "The compliance category to update.", true)

        ComplianceCategory.values().forEach {
            val name = it.name
            option.addChoice(name, name)
        }

        val commandData = CommandData(COMMAND_NAME, "Present options to manage compliance opt-ins/opt-outs.")
            .addOptions(option)

        event.jda.upsertCommand(commandData).queue()
    }

    /**
     * Handle the /compliance commands
     */
    override fun onSlashCommand(event: SlashCommandEvent) {
        scope.launch {
            if (event.commandPath == COMMAND_NAME) {
                event.getOption(OPTION_NAME)?.asString?.let { option ->
                    try {
                        ComplianceCategory.valueOf(option)
                    } catch (e: IllegalArgumentException) {
                        logger.error("Unrecognized option $option", e)
                        null
                    }
                }?.let { complianceCategory ->
                    event.deferReply(true).queue()
                    val optedIn = ComplianceOfficer.check(event.user.idLong, complianceCategory)
                    event.hook.editOriginal(complianceCategory.buildComplianceMessage(optedIn)).queue()
                }
            }
        }
    }

    /**
     * Compliance button handling
     */
    override fun onButtonClick(event: ButtonClickEvent) {
        scope.launch {
            val path = event.button?.id?.split(":")
            if (path?.firstOrNull() == "Compliance") {
                val (_, categoryString, decision) = path
                val category = ComplianceCategory.valueOf(categoryString)
                when (decision) {
                    "In" -> true
                    "Out" -> false
                    else -> null
                }?.let { optedIn ->
                    event.deferReply(true).queue()
                    ComplianceOfficer.decide(event.user.idLong, category, optedIn)
                    val inOut = if (optedIn) "in to" else "out of"
                    logger.info("${event.user} opted $inOut $category")
                    event.hook.sendMessage("You have opted $inOut $category.").queue()
                }
            }
        }
    }
}
