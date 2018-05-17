package me.ianmooreis.glyph.utils.extractors

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL
import java.net.URLEncoder

object WikipediaExtractor {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    data class WikipediaPage(val title: String, val intro: String, val url: URL)

    fun getPage(query: String, failure: () -> Unit, success: (WikipediaPage) -> Unit) {
        val queryUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts%7Cinfo&titles=${URLEncoder.encode(query, "UTF-8")}&redirects=1&exintro=1&explaintext=1&inprop=url&exchars=500"
        queryUrl.httpGet().responseString { _, _, result ->
            when (result) {
                is Result.Success -> {
                    val pages = JSONObject(result.get()).getJSONObject("query").getJSONObject("pages")
                    val page = pages.getJSONObject(pages.keys().next())
                    success(WikipediaPage(page.getString("title"), page.getString("extract"), URL(page.getString("fullurl"))))
                }
                is Result.Failure -> {
                    failure()
                    this.log.info("Failed to find page for query $query!")
                }
            }
        }
    }
}

