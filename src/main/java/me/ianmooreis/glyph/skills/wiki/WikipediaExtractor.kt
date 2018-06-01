package me.ianmooreis.glyph.skills.wiki

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL
import java.net.URLEncoder

object WikipediaExtractor {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)

    fun getArticle(query: String): WikiArticle? {
        val queryUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts%7Cinfo&titles=${URLEncoder.encode(query, "UTF-8")}&redirects=1&exintro=1&explaintext=1&inprop=url&exchars=500"
        val (_, _, result) = queryUrl.httpGet().responseString()
        return when (result) {
            is Result.Success -> {
                try {
                    val pages = JSONObject(result.get()).getJSONObject("query").getJSONObject("pages")
                    val page = pages.getJSONObject(pages.keys().next())
                    val extract = page.getString("extract")
                    if (extract != null && !extract.contains("may refer to")) {
                        WikiArticle(page.getString("title"), extract, URL(page.getString("fullurl")))
                    } else {
                        null
                    }
                } catch (e: JSONException) {
                    null
                }
            }
            is Result.Failure -> {
                log.debug("Failed to find page for query $query!")
                null
            }
        }
    }
}

