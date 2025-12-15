plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.jdbc)
    implementation(libs.jda)
    implementation(libs.jda.ktx)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactive)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.lettuce.core)
    implementation(libs.logback.classic)
    implementation(libs.mariadb)
    implementation(libs.typesafe.config)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.yttr.glyph.bot.GlyphKt")
}

tasks.register("stage") {
    dependsOn(tasks.named("installDist"))
}
