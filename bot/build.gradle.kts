import tanvd.kosogor.proxy.shadowJar

/*
 * build.gradle.kts
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

group = "me.ianmooreis.glyph.bot"
version = "1.0"

val kotlin_version: String by project.extra
val logback_version: String by project.extra
val jda_version: String by project.extra
val exposed_version: String by project.extra
val ktor_version: String by project.extra

shadowJar {
    jar {
        archiveName = "glyph-bot.jar"
        mainClass = "me.ianmooreis.glyph.bot.GlyphKt"
    }
}

tasks.named("stage") {
    dependsOn("shadowJar")
}

plugins {
    kotlin("plugin.serialization") version "1.3.72" apply true
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.7")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.dv8tion:JDA:$jda_version")
    implementation("club.minnced:discord-webhooks:0.3.0")
    implementation("com.google.cloud:google-cloud-storage:1.106.0")
    implementation("com.google.cloud:google-cloud-dialogflow:1.0.0")
    implementation("net.dean.jraw:JRAW:1.0.0")
    implementation("org.ocpsoft.prettytime:prettytime:4.0.4.Final")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation("com.vdurmont:emoji-java:4.0.0")
    implementation("net.jodah:expiringmap:0.5.9")
    implementation("commons-codec:commons-codec:1.14")
}
