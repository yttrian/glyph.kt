/*
 * Skill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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
 */kage org.yttr.glyph.bot.skills

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yttr.glyph.bot.ai.AIResponse
import org.yttr.glyph.bot.extensions.contentClean
import org.yttr.glyph.bot.extensions.isCreator
import org.yttr.glyph.bot.messaging.Response
import org.yttr.glyph.shared.config.server.ServerConfig
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * The definition of a skill with a trigger word, cooldown times, required permissions, and usage limits
 */
abstract class Skill(
    /**
     * The trigger world (a DialogFlow action) to use to refer to the skill
     */
    val trigger: String,
    private val cooldownTime: Long = 2, private val cooldownUnit: TimeUnit = TimeUnit.SECONDS,
    private val guildOnly: Boolean = false,
    private val requiredPermissionsUser: Collection<Permission> = emptyList(),
    private val requiredPermissionsSelf: Collection<Permission> = emptyList(),
    private val creatorOnly: Boolean = false
) {
    // TODO: Avoid using lateinit and var
    lateinit var skillDirector: SkillDirector

    /**
     * The skill's logger which will show the skill's name in the console when logs are made
     */
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * When the skill is triggered
     */
    protected abstract suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response

    /**
     * Trigger the skill but do some checks first before truly triggering it
     */
    suspend fun trigger(event: MessageReceivedEvent, ai: AIResponse): Response {
        val permittedUser: Boolean =
            if (event.channelType.isGuild) event.member!!.hasPermission(requiredPermissionsUser) else true
        val permittedSelf: Boolean =
            if (event.channelType.isGuild) event.guild.selfMember.hasPermission(requiredPermissionsSelf) else true
        val currentCooldown: SkillCooldown? = skillDirector.getCooldown(event.author, this)
        when {
            currentCooldown != null && !currentCooldown.expired ->
                if (!currentCooldown.warned) {
                    log.info("Received \"${event.message.contentClean}\" from ${event.author} ${if (event.channelType.isGuild) "in ${event.guild}" else "in PM"}, cooled")
                    currentCooldown.warned = true
                    return Response.Ephemeral(
                        "⌛ `$trigger` is on cooldown, please wait ${currentCooldown.remainingSeconds} seconds before trying to use it again.",
                        Duration.ofSeconds(currentCooldown.remainingSeconds)
                    )
                } else {
                    event.message.addReaction("⌛").queue() //React with :hourglass: to indicate cooldown
                }
            (guildOnly || requiredPermissionsUser.isNotEmpty()) && !event.channelType.isGuild -> return Response.Volatile(
                "You can only do that in a server!"
            )
            !permittedSelf -> return Response.Volatile(
                "I don't have the required permissions to do that! (${
                    requiredPermissionsSelf.joinToString {
                        prettyPrintPermissionName(it)
                    }
                })"
            )
            !permittedUser -> return Response.Volatile(
                "You don't have the required permissions to do that! (${
                    requiredPermissionsUser.joinToString {
                        prettyPrintPermissionName(it)
                    }
                })"
            )
            creatorOnly && !event.author.isCreator -> event.message.addReaction("❓")
                .queue() //Pretend the skill does not exist
            else -> {
                log.info("Received \"${event.message.contentClean}\" from ${ai.sessionID}")
                skillDirector.setCooldown(event.author, this, SkillCooldown(cooldownTime, cooldownUnit))
                return this.onTrigger(event, ai)
            }
        }

        return Response.None
    }

    private fun prettyPrintPermissionName(permission: Permission): String {
        return permission.name.split("_").joinToString(" ") { it.toLowerCase().capitalize() }
    }

    override fun toString(): String = trigger

    /**
     * Awaits on a RestAction
     */
    protected suspend fun <T> RestAction<T>.await(): T = this.submit().await()

    /**
     * Get the config for a server (or default if none)
     */
    protected val Guild.config: ServerConfig
        get() = skillDirector.configDirector.getServerConfig(this)

    /**
     * Default configuration
     */
    protected val defaultConfig: ServerConfig
        get() = skillDirector.configDirector.defaultConfig
}
