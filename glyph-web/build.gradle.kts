plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":glyph-shared"))

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.mustache)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.webjars)

    implementation(libs.logback.classic)
    runtimeOnly(libs.webjars.bootstrap)

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.register("stage") {
    dependsOn(tasks.named("installDist"))
}
