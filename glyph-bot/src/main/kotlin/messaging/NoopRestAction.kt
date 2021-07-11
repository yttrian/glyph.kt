/*
 * NoopRestAction.kt
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
 */

package org.yttr.glyph.bot.messaging

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier
import java.util.function.Consumer

/**
 * A RestAction that does nothing
 */
class NoopRestAction<T>(private val jda: JDA) : AuditableRestAction<T> {
    private val noop = UnsupportedOperationException("NoopRestAction does not do anything")

    override fun getJDA(): JDA = jda

    override fun setCheck(checks: BooleanSupplier?): AuditableRestAction<T> = this

    override fun queue(success: Consumer<in T>?, failure: Consumer<in Throwable>?) {
        failure?.accept(noop)
    }

    override fun complete(shouldQueue: Boolean): T {
        throw noop
    }

    override fun submit(shouldQueue: Boolean): CompletableFuture<T> = CompletableFuture.failedFuture(noop)

    override fun timeout(timeout: Long, unit: TimeUnit): AuditableRestAction<T> = this

    override fun deadline(timestamp: Long): AuditableRestAction<T> = this

    override fun reason(reason: String?): AuditableRestAction<T> = this
}
