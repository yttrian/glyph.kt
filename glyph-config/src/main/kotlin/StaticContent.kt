package org.yttr.glyph.config

import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.Route

/**
 * Static content for the website. This makes up the majority of user facing pages.
 * Server is only really needed to load/save the config data.
 */
fun Route.staticContent() {
    static {
        static("css") {
            resources("content/css")
        }

        static("img") {
            resources("content/img")
        }

        static("js") {
            resources("content/js")
        }
    }
}
