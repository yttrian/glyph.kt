package org.yttr.glyph.config

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route

/**
 * Static content for the website. This makes up the majority of user facing pages.
 * Server is only really needed to load/save the config data.
 */
fun Route.staticContent() {
    staticResources("css", "content/css")
    staticResources("img", "content/img")
    staticResources("js", "content/js")
}
