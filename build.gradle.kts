plugins {
    alias(libs.plugins.kotlin.jvm)
}

val jdkVersion = 21

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain(jdkVersion)
    }

    sourceSets {
        main {
            kotlin.srcDir("src")
            resources.srcDir("resources")
        }
        test {
            kotlin.srcDir("test")
        }
    }
}
