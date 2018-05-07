package me.ianmooreis.glyph.orchestrators.messaging

import net.dv8tion.jda.core.entities.Emote

enum class CustomEmote(val emote: Emote?) {
    XMARK(MessagingOrchestrator.getCustomEmote("xmark")),
    NOMARK(MessagingOrchestrator.getCustomEmote("empty")),
    CHECKMARK(MessagingOrchestrator.getCustomEmote("checkmark")),
    BOT(MessagingOrchestrator.getCustomEmote("bot")),
    DOWNLOAD(MessagingOrchestrator.getCustomEmote("download")),
    DOWNLOADING(MessagingOrchestrator.getCustomEmote("downloading")),
    LOADING(MessagingOrchestrator.getCustomEmote("loading")),
    TYPING(MessagingOrchestrator.getCustomEmote("typing")),
    ONLINE(MessagingOrchestrator.getCustomEmote("online")),
    STREAMING(MessagingOrchestrator.getCustomEmote("streaming")),
    AWAY(MessagingOrchestrator.getCustomEmote("away")),
    DND(MessagingOrchestrator.getCustomEmote("dnd")),
    OFFLINE(MessagingOrchestrator.getCustomEmote("offline")),
    INVISIBLE(MessagingOrchestrator.getCustomEmote("invisible")),
    THINKING(MessagingOrchestrator.getCustomEmote("thinking")),
    COOL(MessagingOrchestrator.getCustomEmote("cool")),
    EXPLICIT(MessagingOrchestrator.getCustomEmote("explicit")),
    CONFIDENTIAL(MessagingOrchestrator.getCustomEmote("confidential")),
    GRIMACE(MessagingOrchestrator.getCustomEmote("grimace")),
    MINDBLOWN(MessagingOrchestrator.getCustomEmote("mindblown"));

    override fun toString() = emote?.asMention ?: ""
}