package org.yttr.glyph.bot.modules

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.yttr.glyph.shared.pubsub.PubSubChannel
import org.yttr.glyph.shared.pubsub.redis.RedisPubSub

class ConfigModule(private val redisPubSub: RedisPubSub) : Module {
    private val log by SLF4J

    override fun boot(jda: JDA) {
        jda.onCommand("config") { event ->
            event.reply("Visit https://glyph.yttr.dev/config to configure Glyph.").setEphemeral(true).queue()
        }

        redisPubSub.addResponder(askChannelPrefix = PubSubChannel.CONFIG_PREFIX) { guildId ->
            val guild = jda.getGuildById(guildId)
            log.info("Responded to data request for guild $guildId")
            guild?.getServerConfigJson()
        }
    }

    override fun updateCommands(commands: CommandListUpdateAction) {
        commands.slash("config", "Configure Glyph") {
            defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)
            setContexts(InteractionContextType.GUILD)
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
        guild.roleCache.filterNot { it.isPublicRole || it.isManaged }.forEach {
            val roleData = JsonObject()
            roleData.addProperty("id", it.id)
            roleData.addProperty("name", it.name)
            roleData.addProperty("position", it.position)
            roleData.addProperty("canInteract", guild.selfMember.canInteract(it))
            roles.add(roleData)
        }
        serverData.add("roles", roles)

        val textChannels = JsonArray()
        guild.textChannelCache.forEach {
            if (it.canTalk()) {
                val channelData = JsonObject()
                channelData.addProperty("id", it.id)
                channelData.addProperty("name", it.name)
                channelData.addProperty("nsfw", it.isNSFW)
                textChannels.add(channelData)
            }
        }
        serverData.add("textChannels", textChannels)

        val emojis = JsonArray()
        guild.emoteCache.forEach {
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
}
