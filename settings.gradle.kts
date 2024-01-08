pluginManagement {
    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
    }
}

rootProject.name = "glyph"

include("glyph-bot")
include("glyph-config")
include("glyph-shared")
