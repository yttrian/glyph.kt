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
        implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        testImplementation(kotlin("test"))
    }
}
