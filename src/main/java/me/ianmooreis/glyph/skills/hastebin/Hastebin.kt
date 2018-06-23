package me.ianmooreis.glyph.skills.hastebin

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.net.URL

/**
 * A simple wrapper for the Hastebin API
 */
object Hastebin {
    /**
     * Asynchronously send content to Hastebin and get the key and url of the upload, otherwise get the error response
     *
     * @param content content to upload to Hastebin
     * @param timeout a timeout in milliseconds
     * @param handler a callback to run on completion
     */
    fun postHaste(content: String, timeout: Int = 4000, handler: (key: String?, url: URL?, response: Response, result: Result<String, FuelError>) -> Unit) {
        "https://hastebin.com/documents".httpPost().body(content).timeout(timeout).responseString { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val key = JSONObject(result.get()).getString("key")
                    val url = URL("https://hastebin.com/$key")
                    handler(key, url, response, result)
                }
                is Result.Failure -> {
                    handler(null, null, response, result)
                }
            }
        }
    }

    /**
     * Asynchronously get content from Hastebin via a key or url, otherwise get the error response
     *
     * @param reference either the key or the url of the Hastebin document
     * @param timeout a timeout in milliseconds
     * @param handler a callback to run on completion
     */
    fun getHaste(reference: String, timeout: Int = 4000, handler: (response: Response, result: Result<String, FuelError>) -> Unit) {
        val key = reference.split("/").last()
        val url = "https://hastebin.com/raw/$key"
        url.httpGet().timeout(timeout).responseString { _, response, result ->
            handler(response, result)
        }
    }

    /**
     * Post content blocking to Hastebin
     *
     * @param
     *
     * @return the Hastebin document url or null if failed
     */
    fun postHasteBlocking(content: String, timeout: Int = 4000): String? {
        val (_, _, result) = "https://hastebin.com/documents".httpPost().body(content).timeout(timeout).responseString()
        return if (result is Result.Success) {
            val key = JSONObject(result.get()).getString("key")
            "https://hastebin.com/$key"
        } else {
            null
        }
    }
}