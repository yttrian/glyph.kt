package org.yttr.glyph.bot.modules

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.TimeUtil
import org.yttr.glyph.bot.jda.buildReply
import java.awt.Color

class SnowstampModule : Module() {
    override fun register() {
        onCommand("snowstamp") { event -> snowstamp(event) }
    }

    override fun commands(): List<CommandData> = listOf(
        Commands.slash("snowstamp", "Get a timestamp from a snowflake ID")
            .addOption(OptionType.INTEGER, "snowflake", "The snowflake ID to get a timestamp for", true)
    )

    fun snowstamp(event: GenericCommandInteractionEvent) {
        val snowflake = event.getOption("snowflake")?.asLong

        if (snowflake == null) {
            event.buildReply("Please provide a valid snowflake ID!") { ephemeral = true }.queue()
            return
        }

        val snowflakeTime = TimeUtil.getTimeCreated(snowflake)

        event.buildReply {
            embeds += EmbedBuilder()
                .setTitle(snowflake.toString())
                .setDescription(TimeFormat.DATE_TIME_LONG.format(snowflakeTime))
                .setColor(Color.WHITE)
                .setFooter("Snowstamp")
                .setTimestamp(snowflakeTime)
                .build()
        }.queue()
    }
}
