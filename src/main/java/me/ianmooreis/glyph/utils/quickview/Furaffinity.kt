package me.ianmooreis.glyph.utils.quickview

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

data class Submission(val title: String, val author: String, val posted: String, val category: String, val theme: String,
                      val species: String, val gender: String, val favorites: Int, val comments: Int, val views: Int,
                      val rating: String, val link: String, val download: String)

object Furaffinity {
    fun getSubmission(id: Int? = null, url: String? = null): Submission? {
        val submissionId: String? = when {
            id != null -> id.toString()
            url != null -> {
                val urlFormat = Regex("((http[s]?)://)?(www.)?(furaffinity.net)/(\\w*)/(\\d{8})/?", RegexOption.IGNORE_CASE)
                val link = urlFormat.find(url)
                link!!.groups[6]!!.value
            }
            else -> null
        }
        if (submissionId != null) {
            try {
                val request: Request = Request.Builder()
                        .url("http://faexport.boothale.net/submission/$submissionId.json")
                        .get()
                        .build()
                val client = OkHttpClient()
                val submissionInfo: String = client.newCall(request).request().body().toString()
                return Gson().fromJson(submissionInfo, Submission::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}