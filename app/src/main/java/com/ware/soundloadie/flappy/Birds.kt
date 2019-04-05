package com.ware.soundloadie.flappy

import android.content.Context
import android.util.Patterns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat

class Birds {

    @Throws(IOException::class)
    fun readJsonFile(fileName: String,context: Context): String {
        var reader: BufferedReader? = null
        reader = BufferedReader(InputStreamReader(context.assets.open(fileName), "UTF-8"))

        var content = ""
        while (true) {
            var line: String? = reader.readLine() ?: break
            content += line

        }

        return content
    }

    fun jsonMan(urls:String):String{
        val client = OkHttpClient()
        val requests = okhttp3.Request.Builder()
            .url(urls)
            .build()
        val responseString = client.newCall(requests).execute()
        val response = responseString!!.body()!!.string()
        return response
    }



    fun retrofit(url: String): String {
        val client = OkHttpClient()
        val requests = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(requests).execute()

        return response!!.body()!!.string()
    }



    // method to sve Soundcloud Files

    fun fetchSong(clientID:String,permalink:String):Array<String>{

        var songID = ""
        var songTITle = ""
        var fileDownloadLink = ""
        var avatarLink = ""
        var filesize = 0

        try {

            val saver = "https://api.soundcloud.com/resolve.json?url=$permalink&client_id=$clientID"
            val results = retrofit(saver)


            var responeJson: JSONObject? = null

            try {
                responeJson = JSONObject(results)

                songID = responeJson.getString("id")
                songTITle = responeJson.getString("title")
                avatarLink = responeJson.getString("artwork_url")
                filesize  = responeJson.getInt("original_content_size")


            } catch (e: JSONException) {
                e.printStackTrace()


            }

            val ultraviolent = "https://api.soundcloud.com/i1/tracks/$songID/streams?client_id=$clientID"
            var result: String? = null
            try {
                result = retrofit(ultraviolent)

                var responeJsons: JSONObject? = null
                try {
                    responeJsons = JSONObject(result)
                    fileDownloadLink = responeJsons.getString("http_mp3_128_url")


                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            } catch (e: IOException) {
                e.printStackTrace()
            }


        } catch (e: IOException) {
            e.printStackTrace()

        }

        return arrayOf("$fileDownloadLink","$songTITle","$filesize",avatarLink)
    }



    // method to scrape links from strings

    fun scrapeLinks(strings: String): Array<String> {
        val links = ArrayList<String>()
        val m = Patterns.WEB_URL.matcher(strings)
        while (m.find()) {
            val urls = m.group()
            links.add(urls)
        }

        return links.toTypedArray()
    }

    // method to format filesize
    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }


}