plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(project(":glyph-shared"))

    implementation(libs.discord.webhooks)
    implementation(libs.emoji.java)
    implementation(libs.expiringmap)
    implementation(libs.google.cloud.dialogflow)
    implementation(libs.google.cloud.storage)
    implementation(libs.jda)
    implementation(libs.jsoup)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.core)
    implementation(libs.logback.classic)
    implementation(libs.prettytime)
    implementation(libs.typesafe.config)


    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.yttr.glyph.bot.GlyphKt")
}

tasks.register("stage") {
    dependsOn(tasks.named("installDist"))
}
