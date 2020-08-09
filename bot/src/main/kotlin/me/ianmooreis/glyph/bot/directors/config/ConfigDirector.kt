/*
 * ConfigDirector.kt
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2020 by Ian Moore
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

package me.ianmooreis.glyph.bot.directors.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.LongSerializationPolicy
import kotlinx.coroutines.launch
import me.ianmooreis.glyph.bot.Director
import me.ianmooreis.glyph.shared.config.ConfigManager
import me.ianmooreis.glyph.shared.config.server.ServerConfig
import me.ianmooreis.glyph.shared.pubsub.PubSubChannel
import me.ianmooreis.glyph.shared.pubsub.redis.RedisPubSub
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent

/**
 * Manages the configuration database
 */
class ConfigDirector(configure: ConfigManager.Config.() -> Unit) : Director() {
    private val configManager = ConfigManager(configure)

    /**
     * The default server config
     */
    val defaultConfig: ServerConfig = configManager.defaultConfig

    /**
     * JDA instance for grabbing Guild data
     */
    lateinit var jda: JDA

    private val redis = RedisPubSub {
        redisConnectionUri = System.getenv("REDIS_URL")
    }

    /**
     * Sets the JDA instance
     */
    override fun onReady(event: ReadyEvent) {
        jda = event.jda
    }

    init {
        redis.addResponder(PubSubChannel.CONFIG_PREFIX) { guildId ->
            val guild = jda.getGuildById(guildId)
            log.info("Responded to data request for guild $guildId")
            guild?.getServerConfigJson()
        }

        redis.addListener(PubSubChannel.CONFIG_REFRESH) { guildId ->
            guildId.toLongOrNull()?.let {
                configManager.reloadServerConfig(it)
                log.info("Reloaded config for guild $guildId")
            }
        }
    }

    private fun Guild.getServerConfigJson(): String {
        val guild = this
        val serializer = GsonBuilder()
            .serializeNulls()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create()
        val config = serializer.toJsonTree(getServerConfig(guild)).asJsonObject
        val serverData = JsonObject()

        // Let's add some extra useful details about the server for suggestions
        val info = JsonObject()
        info.addProperty("name", guild.name)
        info.addProperty("id", guild.id)
        info.addProperty("icon", guild.iconUrl)
        serverData.add("info", info)

        val roles = JsonArray()
        guild.roles.filterNot { it.isPublicRole || it.isManaged }.forEach {
            val roleData = JsonObject()
            roleData.addProperty("id", it.id)
            roleData.addProperty("name", it.name)
            roleData.addProperty("position", it.position)
            roleData.addProperty("canInteract", guild.selfMember.canInteract(it))
            roles.add(roleData)
        }
        serverData.add("roles", roles)

        val textChannels = JsonArray()
        guild.textChannels.forEach {
            val channelData = JsonObject()
            channelData.addProperty("id", it.id)
            channelData.addProperty("name", it.name)
            channelData.addProperty("nsfw", it.isNSFW)
            textChannels.add(channelData)
        }
        serverData.add("textChannels", textChannels)

        val emojis = JsonArray()
        guild.emotes.forEach {
            val emojiData = JsonObject()
            emojiData.addProperty("id", it.id)
            emojiData.addProperty("name", it.name)
            emojiData.addProperty("image", it.imageUrl)
            emojis.add(emojiData)
        }
        serverData.add("emojis", emojis)

        // Package it all up and send it out
        config.add("_data", serverData)
        return config.toString()
    }

    /**
     * Retrieves the server's config
     */
    fun getServerConfig(guild: Guild): ServerConfig = configManager.getServerConfig(guild.idLong)

    /**
     * Deletes a server's config
     */
    private fun deleteServerConfig(guild: Guild) {
        launch { configManager.deleteServerConfig(guild.idLong) }
    }

    /**
     * Delete a guild's config when the server if left
     */
    override fun onGuildLeave(event: GuildLeaveEvent) {
        launch { deleteServerConfig(event.guild) }
    }
}
