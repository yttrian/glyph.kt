package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import java.awt.Color
import java.time.Instant

class ObservatoryModule(private val webhookId: Long) : Module {
    private val log by SLF4J

    override fun boot(jda: JDA) {
        jda.onCommand("status") { event -> status(event) }
        jda.listener<GuildJoinEvent> { event -> log(event) }
        jda.listener<GuildLeaveEvent> { event -> log(event) }

        ready(jda)
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("status", "Check the operating status of Glyph")
    }

    private fun status(event: GenericCommandInteractionEvent) {
        event.reply(MessageCreate {
            embeds += Embed {
                title = "Glyph Status"
                field(name = "Ping", value = "${event.jda.gatewayPing} ms")
                field(name = "Servers", value = "${event.jda.guildCache.size()}")
            }
        }).queue()
    }

    private fun ready(jda: JDA) {
        jda.presence.setPresence(OnlineStatus.ONLINE, Activity.customStatus("glyph.yttr.org"))

        val shard = jda.shardInfo
        log.info("Ready on shard ${shard.shardId}/${shard.shardTotal} with ${jda.guilds.size} guilds")
    }

    private suspend fun log(event: GenericGuildEvent) {
        val webhook = event.jda.retrieveWebhookById(webhookId).await()

        val message = MessageCreate {
            embeds += Embed {
                when (event) {
                    is GuildJoinEvent -> {
                        title = "Joined Guild"
                        color = Color.GREEN.rgb
                    }

                    is GuildLeaveEvent -> {
                        title = "Left Guild"
                        color = Color.RED.rgb
                    }
                }

                description = """
                    **Name**: ${event.guild.name}
                    **ID**: ${event.guild.id}
                    **Members**: ${event.guild.memberCount}
                """.trimIndent()

                thumbnail = event.guild.iconUrl

                footer {
                    content = "Logging"
                    timestamp = Instant.now()
                }
            }
        }

        webhook.sendMessage(message).queue()
    }
}
