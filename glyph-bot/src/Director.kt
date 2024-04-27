package org.yttr.glyph.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.RestAction
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yttr.glyph.bot.skills.config.ConfigDirector
import org.yttr.glyph.shared.config.server.ServerConfig
import kotlin.coroutines.CoroutineContext

/**
 * The definition of a director, with pre-included properties like a logger
 */
abstract class Director : ListenerAdapter(), CoroutineScope {
    /**
     * The directors's logger which will show the director's name in the console when logs are made
     */
    protected val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override val coroutineContext: CoroutineContext = SupervisorJob()

    lateinit var configDirector: ConfigDirector

    /**
     * md5hex of the author id concatenated with the channel id to identify a context without being too revealing
     */
    protected val MessageReceivedEvent.contextHash: String
        get() = DigestUtils.md5Hex(author.id + channel.id)

    protected val Guild.config: ServerConfig
        get(): ServerConfig = configDirector.getServerConfig(this)

    /**
     * Submit and await a RestAction in a suspending context
     */
    protected suspend fun <T> RestAction<T>.await(): T = submit().await()
}
