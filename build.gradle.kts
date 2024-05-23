plugins {
    application
}

application {
    mainClass.set("org.yttr.glyph.bot.GlyphKt")
}

tasks.create("stage") {
    dependsOn("installDist")
}
