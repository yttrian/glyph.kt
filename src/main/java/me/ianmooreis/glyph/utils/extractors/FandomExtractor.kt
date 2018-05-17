package me.ianmooreis.glyph.utils.extractors

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.net.URL
import java.net.URLEncoder

object FandomExtractor {
    private val log : Logger = SimpleLoggerFactory().getLogger(this.javaClass.simpleName)
    data class FandomPage(val title: String, val intro: String, val url: URL)

    fun getPage(wiki: String, query: String, minimumQuality: Int, failure: () -> Unit, success: (FandomPage) -> Unit) {
        val searchUrl = "https://${URLEncoder.encode(wiki, "UTF-8")}.wikia.com/" +
                "api/v1/Search/List?query=${URLEncoder.encode(query, "UTF-8")}" +
                "&limit=1&minArticleQuality=${URLEncoder.encode(minimumQuality.toString(), "UTF-8")}&batch=1&namespaces=0%2C14"
        searchUrl.httpGet().responseString {  _, _, searchResult ->
            when (searchResult) {
                is Result.Success -> {
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
                    success(FandomPage(page.getString("title"), snippet, URL(page.getString("url"))))
                }
                is Result.Failure -> {
                    failure()
                    this.log.info("Failed to find page for query $query!")
                }
            }
        }
    }
}