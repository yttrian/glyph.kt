package me.ianmooreis.glyph.utils.libraries

import com.github.kittinunf.fuel.httpGet
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL

object WikipediaExtractor {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    data class WikipediaPage(val title: String, val intro: String, val url: URL)

    fun getPage(query: String) : WikipediaPage? {
        try {
            val queryUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts%7Cinfo&titles=$query&redirects=1&exintro=1&explaintext=1&inprop=url&exchars=500"
            val (_, response, result) = queryUrl.httpGet().responseString()
            if (response.statusCode == 200) {
                val pages = JSONObject(result.get()).getJSONObject("query").getJSONObject("pages")
                val page = pages.getJSONObject(pages.keys().next())
                return WikipediaPage(page.getString("title"), page.getString("extract"), URL(page.getString("fullurl")))
            }
        } catch (e: JSONException) {
            this.log.info("Failed to find page for query $query!")
        }
        return null
    }
}

