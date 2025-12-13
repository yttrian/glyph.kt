package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.TimeUtil
import java.awt.Color

class SnowstampModule : Module {
    override fun boot(jda: JDA) {
        jda.onCommand("snowstamp") { event -> snowstamp(event) }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("snowstamp", "Get a timestamp from a snowflake ID") {
            option<Long>("snowflake", "The snowflake ID to get a timestamp for", true)
        }
    }

    fun snowstamp(event: GenericCommandInteractionEvent) {
        val snowflake = event.getOption("snowflake")?.asLong

        if (snowflake == null) {
            event.reply("Please provide a valid snowflake ID!").setEphemeral(true).queue()
            return
        }

        val message = MessageCreate {
            val snowflakeTime = TimeUtil.getTimeCreated(snowflake)

            embeds += EmbedBuilder()
                .setTitle(snowflake.toString())
                .setDescription(TimeFormat.DATE_TIME_LONG.format(snowflakeTime))
                .setColor(Color.WHITE)
                .setFooter("Snowstamp")
                .setTimestamp(snowflakeTime)
                .build()
        }

        event.reply(message).queue()
    }
}
