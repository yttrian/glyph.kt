/*
 * build.gradle.kts
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2021 by Ian Moore
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

group = "org.yttr.glyph.bot"
version = "1.0"

internal val coroutinesVersion: String by project.extra
internal val logbackVersion: String by project.extra
internal val jdaVersion: String by project.extra
internal val ktorVersion: String by project.extra

repositories {
    maven("https://m2.dv8tion.net/releases")
}

shadowJar {
    jar {
        archiveName = "glyph-bot.jar"
        mainClass = "org.yttr.glyph.bot.GlyphKt"
    }
}.task.mergeServiceFiles()

tasks.named("stage") {
    dependsOn("shadowJar")
}

dependencies {
    implementation(project(":glyph-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("club.minnced:discord-webhooks:0.5.7")
    implementation("com.google.cloud:google-cloud-storage:1.106.0")
    implementation("com.google.cloud:google-cloud-dialogflow:1.0.0")
    implementation("net.dean.jraw:JRAW:1.1.0")
    implementation("org.ocpsoft.prettytime:prettytime:4.0.4.Final")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
    implementation("com.vdurmont:emoji-java:4.0.0")
    implementation("net.jodah:expiringmap:0.5.9")
    implementation("commons-codec:commons-codec:1.14")
    implementation("com.typesafe:config:1.4.1")
}
