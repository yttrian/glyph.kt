plugins {
    application
}

application {
    mainClass.set("org.yttr.glyph.GlyphKt")
}

tasks.create("stage") {
    dependsOn("installDist")
}
