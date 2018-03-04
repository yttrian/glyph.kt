package me.ianmooreis.glyph.utils.libraries

import com.github.kittinunf.fuel.httpGet
import org.json.JSONException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL

object FandomExtractor {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    data class FandomPage(val title: String, val intro: String, val url: URL)

    fun getPage(wiki: String, query: String) : FandomPage? {
        try {
            val searchUrl = "http://$wiki.wikia.com/api/v1/Search/List?query=${query.replace(" ", "%20")}&limit=1&minArticleQuality=50&batch=1&namespaces=0%2C14"
            val (_, searchResponse, searchResult) = searchUrl.httpGet().responseString()
            if (searchResponse.statusCode == 200) {
                val page = JSONObject(searchResult.get()).getJSONArray("items").getJSONObject(0)
                val snippet = try {
                    val pageUrl = "http://$wiki.wikia.com/api/v1/Articles/AsSimpleJson?id=${page.getInt("id")}"
                    val (_, pageResponse, pageResult) = pageUrl.httpGet().responseString()
                    if (pageResponse.statusCode == 200) {
                        JSONObject(pageResult.get()).getJSONArray("sections").getJSONObject(0)
                                .getJSONArray("content").getJSONObject(0)
                                .getString("text")
                    } else {
                        throw Exception("No page text found!")
                    }
                } catch (e: Exception) {
                    log.warn(e.message)
                    page.getString("snippet")
                }
                return FandomPage(page.getString("title"), snippet, URL(page.getString("url")))
            }
        } catch (e: JSONException) {
            this.log.info("Failed to find page for query $query!")
        }
        return null
    }
}