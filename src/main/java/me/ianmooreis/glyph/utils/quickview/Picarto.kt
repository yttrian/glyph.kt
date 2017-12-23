package me.ianmooreis.glyph.utils.quickview

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

enum class Status(val status: String, val color: Color) {
    ONLINE("Online", Color.GREEN),
    OFFLINE("Offline", Color.RED)
}

data class Channel(val name: String, var viewers: Int, val category: String, val title: String, var status: Status, var adult: Boolean)

object Picarto {
    fun getChannel(name: String? = null, url: String? = null): Channel? {
        val channelName: String? = when {
            name != null -> name
            url != null -> {
                val urlFormat = Regex("((http[s]?)://)?(www.)?(picarto.tv)/(\\w*)/?", RegexOption.IGNORE_CASE)
                val link = urlFormat.find(url)
                link!!.groups[5]!!.value
            }
            else -> null
        }
        if (channelName != null) {
            try {
                val request: Request = Request.Builder()
                        .url("https://api.picarto.tv/v1/channel/name/$channelName")
                        .get()
                        .build()
                val client = OkHttpClient()
                val channelInfo = JSONObject(client.newCall(request).request().body().toString())
                return Channel(
                        channelInfo.getString("name"),
                        channelInfo.getInt("viewers"),
                        channelInfo.getString("category"),
                        channelInfo.getString("title"),
                        if (channelInfo.getBoolean("online")) Status.ONLINE else Status.OFFLINE,
                        channelInfo.getBoolean("adult"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}