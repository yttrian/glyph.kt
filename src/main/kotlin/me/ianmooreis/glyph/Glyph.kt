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

import me.ianmooreis.glyph.directors.AuditingDirector
import me.ianmooreis.glyph.directors.ServerDirector
import me.ianmooreis.glyph.directors.StarboardDirector
import me.ianmooreis.glyph.directors.StatusDirector
import me.ianmooreis.glyph.directors.messaging.MessagingDirector
import me.ianmooreis.glyph.directors.messaging.quickview.QuickviewDirector
import me.ianmooreis.glyph.directors.skills.SkillDirector
import me.ianmooreis.glyph.skills.*
import me.ianmooreis.glyph.skills.configuration.ServerConfigSkill
import me.ianmooreis.glyph.skills.creator.ChangeStatusSkill
import me.ianmooreis.glyph.skills.creator.FarmsSkill
import me.ianmooreis.glyph.skills.moderation.*
import me.ianmooreis.glyph.skills.roles.RoleListSkill
import me.ianmooreis.glyph.skills.roles.RoleSetSkill
import me.ianmooreis.glyph.skills.roles.RoleUnsetSkill
import me.ianmooreis.glyph.skills.wiki.WikiSkill
import net.dv8tion.jda.core.AccountType
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
            MessagingDirector, AuditingDirector, ServerDirector,
            QuickviewDirector, StatusDirector, StarboardDirector
        )
    }
}

/**
 * Where everything begins
 * Registers all the skills and builds the clients with optional sharding
 */
fun main() {
    SkillDirector.addSkill(
        HelpSkill, StatusSkill, SourceSkill,
        RoleSetSkill, RoleUnsetSkill, RoleListSkill,
        ServerConfigSkill,
        PurgeSkill, UserInfoSkill, GuildInfoSkill, KickSkill, BanSkill, RankSkill,
        EphemeralSaySkill, RedditSkill, WikiSkill, TimeSkill, FeedbackSkill, DoomsdayClockSkill, SnowstampSkill,
        ChangeStatusSkill, FarmsSkill,
        FallbackSkill
    )
    val shardTotal = System.getenv("SHARD_TOTAL").toInt()
    for (i in 0 until shardTotal) {
        Glyph.useSharding(i, shardTotal).build()
        Thread.sleep(5000)
    }
}