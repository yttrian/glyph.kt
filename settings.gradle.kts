rootProject.name = "glyph"

include("glyph-bot")
include("glyph-web")
include("glyph-shared")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
