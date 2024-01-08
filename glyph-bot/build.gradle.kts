plugins {
    application
}

application {
    mainClass.set("org.yttr.glyph.bot.GlyphKt")
}

tasks.create("stage") {
    dependsOn("installDist")
}

repositories {
    jcenter()
}

internal val coroutinesVersion: String by project
internal val jdaVersion: String by project
internal val ktorVersion: String by project

dependencies {
    implementation(project(":glyph-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
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
