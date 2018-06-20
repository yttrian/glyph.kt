package me.ianmooreis.glyph.orchestrators.messaging.quickview.picarto

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import me.ianmooreis.glyph.extensions.contentClean
import me.ianmooreis.glyph.extensions.reply
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory

/**
 * Handles the creation of QuickViews for picarto.tv links
 */
object Picarto {
    private val log: Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    private val urlFormat = Regex("((http[s]?)://)?(www.)?(picarto.tv)/(\\w*)/?", RegexOption.IGNORE_CASE)

    /**
     * Makes any QuickViews for links found in a message
     *
     * @param event the message event
     */
    fun makeQuickviews(event: MessageReceivedEvent) {
        urlFormat.findAll(event.message.contentClean)
            .map { getChannel(it.groups[5]!!.value) }
            .forEach {
                if (it != null) {
                    event.message.reply(it.getEmbed())
                    log.info("Created picarto QuickView in ${event.guild} for ${it.name}")
                }
            }
    }

    private fun getChannel(name: String): Channel? { //TODO: Figure out how not to do it blocking, because async had errors
        val (_, _, result) = "https://api.picarto.tv/v1/channel/name/$name".httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                Gson().fromJson(result.get(), Channel::class.java)
            }
            is Result.Failure -> {
                log.warn("Failed to get channel $name from picarto!")
                return null
            }
        }
    }
}