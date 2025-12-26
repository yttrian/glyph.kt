package org.yttr.glyph.bot.wrappers

///**
// * A skill that shows users the current status of the client, with extra info for the creator only
// */
//class StatusSkill(
//    /**
//     * Redis async connection
//     */
//    private val redis: RedisAsync
//) : Skill("skill.status", cooldownTime = 5) {
//    override suspend fun onTrigger(event: MessageReceivedEvent, ai: AIResponse): Response {
//        val jda = event.jda
//        val name = jda.selfUser.name
//        val discordDescription = SimpleDescriptionBuilder()
//            .addField("Ping", "${jda.gatewayPing} ms")
//            .addField("Guilds", jda.guilds.size)
//            .addField(
//                "Shard",
//                "${jda.shardInfo.shardId}" + if (event.author.isCreator) "/${jda.shardInfo.shardTotal}" else ""
//            )
//        if (event.author.isCreator) {
//            val messageCount = redis.get(MessagingDirector.MESSAGE_COUNT_KEY).await()
//            discordDescription.addField("Messages", messageCount ?: "?")
//        }
//        val embed = EmbedBuilder()
//            .setTitle("$name Status")
//            .addField("Discord", discordDescription.build(), true)
//            .setFooter("$name-Kotlin-${Glyph.version}", null)
//            .setTimestamp(Instant.now())
//        if (event.author.isCreator) {
//            val runtime = Runtime.getRuntime()
//            val usedMemory = "%.2f".format((runtime.totalMemory() - runtime.freeMemory()).toFloat() / 1000000)
//            val maxMemory = "%.2f".format(runtime.maxMemory().toFloat() / 1000000)
//            val uptime = PrettyTime().format(Date(ManagementFactory.getRuntimeMXBean().startTime))
//            val dynoDescription = SimpleDescriptionBuilder()
//                .addField("Cores", runtime.availableProcessors())
//                .addField("Memory", "$usedMemory of $maxMemory MB")
//                .addField("JVM", Runtime.version().toString())
//                .addField("Kotlin", KotlinVersion.CURRENT.toString())
//                .addField("JDA", JDAInfo.VERSION)
//                .addField("Restarted", uptime)
//                .build()
//            embed.addField("Dyno", dynoDescription, true)
//        } else {
//            embed.setThumbnail(jda.selfUser.avatarUrl)
//        }
//        embed.addField("Operating Parameters", ai.result.fulfillment.speech, true)
//
//        return Response.Volatile(embed.build())
//    }
//}
