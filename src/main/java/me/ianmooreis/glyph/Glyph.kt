/*
 * Glyph.kt
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

package me.ianmooreis.glyph

import me.ianmooreis.glyph.orchestrators.AuditingOrchestrator
import me.ianmooreis.glyph.orchestrators.ServerOrchestrator
import me.ianmooreis.glyph.orchestrators.StarboardOrchestrator
import me.ianmooreis.glyph.orchestrators.StatusOrchestrator
import me.ianmooreis.glyph.orchestrators.messaging.MessagingOrchestrator
import me.ianmooreis.glyph.orchestrators.skills.SkillOrchestrator
import me.ianmooreis.glyph.skills.DoomsdayClockSkill
import me.ianmooreis.glyph.skills.EphemeralSaySkill
import me.ianmooreis.glyph.skills.FallbackSkill
import me.ianmooreis.glyph.skills.FeedbackSkill
import me.ianmooreis.glyph.skills.HelpSkill
import me.ianmooreis.glyph.skills.RankSkill
import me.ianmooreis.glyph.skills.RedditSkill
import me.ianmooreis.glyph.skills.SnowstampSkill
import me.ianmooreis.glyph.skills.SourceSkill
import me.ianmooreis.glyph.skills.StatusSkill
import me.ianmooreis.glyph.skills.TimeSkill
import me.ianmooreis.glyph.skills.configuration.ServerConfigGetSkill
import me.ianmooreis.glyph.skills.configuration.ServerConfigSetSkill
import me.ianmooreis.glyph.skills.creator.ChangeStatusSkill
import me.ianmooreis.glyph.skills.creator.FarmsSkill
import me.ianmooreis.glyph.skills.creator.ReloadConfigsSkill
import me.ianmooreis.glyph.skills.moderation.BanSkill
import me.ianmooreis.glyph.skills.moderation.GuildInfoSkill
import me.ianmooreis.glyph.skills.moderation.KickSkill
import me.ianmooreis.glyph.skills.moderation.PurgeSkill
import me.ianmooreis.glyph.skills.moderation.UserInfoSkill
import me.ianmooreis.glyph.skills.roles.RoleListSkill
import me.ianmooreis.glyph.skills.roles.RoleSetSkill
import me.ianmooreis.glyph.skills.roles.RoleUnsetSkill
import me.ianmooreis.glyph.skills.wiki.WikiSkill
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder

/**
 * The Glyph object to use when building the client
 */
object Glyph : JDABuilder(AccountType.BOT) {
    /**
     * The current version of Glyph
     */
    val version: String = System.getenv("HEROKU_RELEASE_VERSION") ?: "?"

    init {
        this.setToken(System.getenv("DISCORD_TOKEN")).addEventListener(
            MessagingOrchestrator, AuditingOrchestrator, ServerOrchestrator, StatusOrchestrator, StarboardOrchestrator)
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main(args: Array<String>) {
    SkillOrchestrator.addSkill(
        HelpSkill, StatusSkill, SourceSkill,
        RoleSetSkill, RoleUnsetSkill, RoleListSkill,
        ServerConfigGetSkill, ServerConfigSetSkill,
        PurgeSkill, UserInfoSkill, GuildInfoSkill, KickSkill, BanSkill, RankSkill,
        EphemeralSaySkill, RedditSkill, WikiSkill, TimeSkill, FeedbackSkill, DoomsdayClockSkill, SnowstampSkill,
        ReloadConfigsSkill, ChangeStatusSkill, FarmsSkill,
        FallbackSkill)
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0..(shardTotal - 1)) {
        Glyph.useSharding(i, shardTotal).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION)
        Thread.sleep(5000)
    }
}