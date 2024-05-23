package org.yttr.glyph.messaging

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
import org.yttr.glyph.compliance.ComplianceCategory
import org.yttr.glyph.compliance.ComplianceOfficer

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
