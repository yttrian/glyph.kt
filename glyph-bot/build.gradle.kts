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

    implementation(libs.jda)
    implementation(libs.jda.ktx)
    implementation(libs.jsoup)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.typesafe.config)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.yttr.glyph.bot.GlyphKt")
}

tasks.register("stage") {
    dependsOn(tasks.named("installDist"))
}
