/*
 * Skill.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2018 by Ian Moore
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

package me.ianmooreis.glyph.directors.skills

import me.ianmooreis.glyph.ai.AIResponse
import me.ianmooreis.glyph.directors.messaging.CustomEmote
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.isCreator
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    /**
     * The skill's logger which will show the skill's name in the console when logs are made
     */
    val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    /**
     * When the skill is triggered
     */
    open fun onTrigger(event: MessageReceivedEvent, ai: AIResponse) {
        this.log.warn("This skill does nothing!")
    }

    /**
     * Trigger the skill but do some checks first before truly triggering it
     */
    fun trigger(event: MessageReceivedEvent, ai: AIResponse) {
        val permittedUser: Boolean = if (event.channelType.isGuild) event.member!!.hasPermission(requiredPermissionsUser) else true
        val permittedSelf: Boolean = if (event.channelType.isGuild) event.guild.selfMember.hasPermission(requiredPermissionsSelf) else true
        val currentCooldown: SkillCooldown? = SkillDirector.getCooldown(event.author, this)
        when {
            currentCooldown != null && !currentCooldown.expired ->
                if (!currentCooldown.warned) {
                    event.message.reply(
                            "⌛ `$trigger` is on cooldown, please wait ${currentCooldown.remainingSeconds} seconds before trying to use it again.",
                            deleteAfterDelay = currentCooldown.remainingSeconds
                    )
                    log.info("Received \"${event.message.contentClean}\" from ${event.author} ${if (event.channelType.isGuild) "in ${event.guild}" else "in PM"}, cooled")
                    currentCooldown.warned = true
                } else {
                    event.message.addReaction("⌛").queue() //React with :hourglass: to indicate cooldown
                }
            (guildOnly || requiredPermissionsUser.isNotEmpty()) && !event.channelType.isGuild -> event.message.reply("${CustomEmote.XMARK} You can only do that in a server!")
            !permittedSelf -> event.message.reply("${CustomEmote.XMARK} I don't have the required permissions to do that! (${requiredPermissionsSelf.joinToString {
                prettyPrintPermissionName(it)
            }})")
            !permittedUser -> event.message.reply("${CustomEmote.XMARK} You don't have the required permissions to do that! (${requiredPermissionsUser.joinToString {
                prettyPrintPermissionName(it)
            }})")
            creatorOnly && !event.author.isCreator -> event.message.addReaction("❓").queue() //Pretend the skill does not exist
            else -> {
                this.onTrigger(event, ai)
                log.info("Received \"${event.message.contentClean}\" from ${ai.sessionID}")
                SkillDirector.setCooldown(event.author, this, SkillCooldown(cooldownTime, cooldownUnit))
            }
        }
    }

    private fun prettyPrintPermissionName(permission: Permission): String {
        return permission.name.split("_").joinToString(" ") { it.toLowerCase().capitalize() }
    }

    override fun toString(): String = trigger
}