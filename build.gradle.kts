/*
 * build.gradle.kts
 *
 * Glyph, a Discord bot that uses natural language instead of commands
 * powered by DialogFlow and Kotlin
 *
 * Copyright (C) 2017-2022 by Ian Moore
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

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://m2.dv8tion.net/releases")
    }
}

val jdaVersion: String by project
val logbackVersion: String by project

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

    group = "org.yttr.glyph"
    version = "1.0.0"

    dependencies {
        implementation("net.dv8tion:JDA:$jdaVersion")
        implementation("io.lettuce:lettuce-core:6.0.0.M1")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        testImplementation(kotlin("test"))
    }
}
