package org.yttr.glyph.bot.presentation

import com.google.gson.JsonParser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import org.yttr.glyph.bot.Director
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * Manages the status messages of the client
 */
object StatusDirector : Director() {
    private val statuses: List<Activity> by lazy {
        val configFile = this.javaClass.classLoader.getResourceAsStream("activities.json")?.reader()
        val config = JsonParser().parse(configFile).asJsonObject
        val types = enumValues<Activity.ActivityType>()

        types.flatMap { type ->
            config.getAsJsonArray(type.name.toLowerCase())?.map {
                Activity.of(type, it.asString)
            } ?: emptyList()
        }
    }

    /**
     * When the client is ready
     */
    override fun onReady(event: ReadyEvent) {
        log.info("Ready on shard ${event.jda.shardInfo.shardId}/${event.jda.shardInfo.shardTotal} with ${event.jda.guilds.count()} guilds")

        // Automatically update the status every hour
        val hour = 3.6e+6.toLong()
        Timer().schedule(0, hour) {
            event.jda.presence.setPresence(OnlineStatus.ONLINE, getRandomStatus())
        }
    }

    /**
     * Change the presence of the client
     */
    fun setPresence(jda: JDA, status: OnlineStatus = jda.presence.status, game: Activity? = jda.presence.activity) {
        jda.presence.setPresence(status, game)
    }

    private fun getRandomStatus(): Activity = statuses.random()
}
